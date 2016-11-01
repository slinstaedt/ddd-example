package ddd.aggregate;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ddd.aggregate.Recorded.CommitResult;
import ddd.aggregate.Recorded.Committed;
import ddd.aggregate.Recorded.Uncommitted;

public class FileBasedEventStore implements EventStore {

	public static ObjectOutputStream append(File file) throws IOException {
		boolean append = file.exists() && file.length() > 0;
		FileOutputStream fos = new FileOutputStream(file, true);
		return append ? new AppendingObjectOutputStream(fos) : new ObjectOutputStream(fos);
	}

	private static class AppendingObjectOutputStream extends ObjectOutputStream {

		public AppendingObjectOutputStream(OutputStream out) throws IOException {
			super(out);
		}

		@Override
		protected void writeStreamHeader() throws IOException {
			reset();
		}
	}

	private class FileEventLog<E> implements EventLog<E> {

		private class FileAggregateLog implements AggregateLog<E> {

			private final Identifier<E> identifier;
			private final File file;

			FileAggregateLog(Identifier<E> identifier) {
				this.identifier = Objects.requireNonNull(identifier);
				this.file = logs.resolve(identifier.value()).toFile();
			}

			@Override
			public Identifier<E> getIdentifier() {
				return identifier;
			}

			@Override
			public Version lastCommittedVersion() {
				return commits().reduce((a, b) -> b).map(Committed::getVersion).orElse(Version.INITIAL);
			}

			@Override
			public CommitResult<E> commit(Uncommitted<E> attempt) {
				Version actual = lastCommittedVersion();
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					try {
						file.createNewFile();
					} catch (IOException e) {
						throw new RuntimeException("could not create file " + file, e);
					}
				}
				return attempt.createCommit(actual).visitCommitted(c -> {
					try (ObjectOutputStream oos = append(file)) {
						oos.writeObject(c);
					} catch (IOException e) {
						throw new RuntimeException("could not commit to file " + file, e);
					}
				});
			}

			@Override
			public Stream<Committed<E>> commits() {
				if (file.exists()) {
					try {
						return toStream(new ObjectInputStream(new FileInputStream(file)));
					} catch (IOException e) {
						throw new RuntimeException("can not open file " + file, e);
					}
				} else {
					return Stream.empty();
				}
			}
		}

		private final Class<E> eventType;
		private final Path logs;

		FileEventLog(Class<E> eventType) {
			this.eventType = Objects.requireNonNull(eventType);
			this.logs = base.resolve(eventType.getCanonicalName());
		}

		@Override
		public Class<E> getEventType() {
			return eventType;
		}

		@Override
		public AggregateLog<E> of(Identifier<E> identifier) {
			return new FileAggregateLog(identifier);
		}

		@Override
		public Stream<AggregateLog<E>> logs() {
			return StreamSupport.stream(logs.spliterator(), false)
					.map(f -> new FileAggregateLog(Identifier.<E>fromValue(logs.getFileName().toString(), f.getFileName().toString())));
		}
	}

	private static final Logger LOGGER = Logger.getLogger(FileBasedEventStore.class.getName());

	public <T> Stream<T> toStream(ObjectInputStream stream) {
		return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {

			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				try {
					@SuppressWarnings("unchecked")
					T entry = (T) stream.readObject();
					action.accept(entry);
					return true;
				} catch (EOFException e) {
					close(stream);
					return false;
				} catch (Exception e) {
					close(stream);
					throw new RuntimeException("could not read stream " + stream, e);
				}
			}

			private void close(InputStream stream) {
				try {
					stream.close();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Couldn't close " + stream, e);
				}
			}
		}, false);
	}

	private final Path base;

	public FileBasedEventStore() {
		this.base = Paths.get("").toAbsolutePath();
	}

	@Override
	public <E> EventLog<E> get(Class<E> eventType) {
		return new FileEventLog<>(eventType);
	}
}