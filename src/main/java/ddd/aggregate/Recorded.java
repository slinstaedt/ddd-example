package ddd.aggregate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;

public interface Recorded<E> {

	interface CommitResult<E> {

		<X> X map(Function<Committed<E>, X> committed, Function<Conflict<E>, X> conflict);

		default CommitResult<E> visitCommitted(Consumer<Committed<E>> committed) {
			return map(c -> {
				committed.accept(c);
				return this;
			}, c -> {
				return this;
			});
		}

		default CommitResult<E> visitCommitted(Runnable committed) {
			return visitCommitted(c -> {
				committed.run();
			});
		}
	}

	@Value
	class Committed<E> implements Recorded<E>, CommitResult<E>, Serializable {

		private static final long serialVersionUID = 1L;

		Identifier<E> identifier;
		Version version;
		LocalDateTime timestamp;
		List<E> events;

		public Stream<E> events() {
			return events.stream();
		}

		public <X> Optional<Committed<X>> asOf(Class<X> type) {
			if (events().allMatch(type::isInstance)) {
				return identifier.asOf(type).map(id -> new Committed<>(id, version, timestamp, events().map(type::cast).collect(Collectors.toList())));
			} else {
				return Optional.empty();
			}
		}

		public boolean after(Version since) {
			return version.afterOrEqual(since);
		}

		@Override
		public <X> X map(Function<Committed<E>, X> committed, Function<Conflict<E>, X> conflict) {
			return committed.apply(this);
		}
	}

	@Value
	class Conflict<E> implements CommitResult<E> {

		Identifier<E> identifier;
		Version expected;
		Version actual;

		@Override
		public <X> X map(Function<Committed<E>, X> committed, Function<Conflict<E>, X> conflict) {
			return conflict.apply(this);
		}
	}

	@Value
	class Uncommitted<E> implements Recorded<E> {

		Identifier<E> identifier;
		Version expected;
		List<E> events;

		public CommitResult<E> createCommit(Version actual) {
			if (expected.equals(actual)) {
				return new Recorded.Committed<>(identifier, expected.next(), LocalDateTime.now(), events);
			} else {
				return new Conflict<>(identifier, expected, actual);
			}
		}
	}

	static <E> Uncommitted<E> uncommitted(Identifier<E> identifier, Version expected, List<E> events) {
		return new Uncommitted<>(identifier, expected, new ArrayList<>(events));
	}
}