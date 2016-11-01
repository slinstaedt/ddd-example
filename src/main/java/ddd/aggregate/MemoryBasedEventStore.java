package ddd.aggregate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import ddd.aggregate.Recorded.CommitResult;
import ddd.aggregate.Recorded.Committed;
import ddd.aggregate.Recorded.Uncommitted;

public class MemoryBasedEventStore implements EventStore {

	private static class MemoryAggregateLog<E> implements AggregateLog<E> {

		private final Identifier<E> identifier;
		private final List<Committed<E>> commits;

		MemoryAggregateLog(Identifier<E> identifier) {
			this.identifier = Objects.requireNonNull(identifier);
			this.commits = new CopyOnWriteArrayList<>();
		}

		@Override
		public Version lastCommittedVersion() {
			return commits.isEmpty() ? Version.INITIAL : commits.get(commits.size() - 1).getVersion();
		}

		@Override
		public synchronized CommitResult<E> commit(Uncommitted<E> attempt) {
			CommitResult<E> result = attempt.createCommit(lastCommittedVersion());
			result.visitCommitted(commits::add);
			return result;
		}

		@Override
		public Stream<Committed<E>> commits() {
			return commits.stream();
		}

		@Override
		public Identifier<E> getIdentifier() {
			return identifier;
		}
	}

	private static class MemoryEventLog<E> implements EventLog<E> {

		private final Class<E> eventType;
		private final ConcurrentMap<Identifier<E>, AggregateLog<E>> logs;

		MemoryEventLog(Class<E> eventType) {
			this.eventType = Objects.requireNonNull(eventType);
			this.logs = new ConcurrentHashMap<>();
		}

		@Override
		public Class<E> getEventType() {
			return eventType;
		}

		@Override
		public Stream<AggregateLog<E>> logs() {
			return logs.values().stream();
		}

		@Override
		public AggregateLog<E> of(Identifier<E> identifier) {
			return logs.computeIfAbsent(identifier, MemoryAggregateLog<E>::new);
		}
	}

	private static final ConcurrentHashMap<Identifier<?>, AggregateLog<?>> EMPTY = new ConcurrentHashMap<>();

	private final ConcurrentMap<Class<?>, EventLog<?>> logs;

	public MemoryBasedEventStore() {
		this.logs = new ConcurrentHashMap<>();
	}

	@Override
	public <E> EventLog<E> get(Class<E> eventType) {
		return eventLog(eventType, false);
	}

	@Override
	public <E> AggregateLog<E> get(Identifier<E> identifier) {
		return eventLog(identifier.getEventType(), true).of(identifier);
	}

	@SuppressWarnings("unchecked")
	private <E> EventLog<E> eventLog(Class<E> eventType, boolean create) {
		if (create) {
			return (EventLog<E>) logs.computeIfAbsent(eventType, MemoryEventLog::new);
		} else {
			return (EventLog<E>) logs.getOrDefault(eventType, (EventLog<?>) EMPTY);
		}
	}
}