package ddd.aggregate;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import ddd.aggregate.Recorded.CommitResult;
import ddd.aggregate.Recorded.Committed;
import ddd.aggregate.Recorded.Uncommitted;

public interface EventStore {

	interface AggregateLog<E> {

		@SuppressWarnings("unchecked")
		default <X> AggregateLog<X> casted(Identifier<X> identifier) {
			if (getIdentifier().equals(identifier)) {
				return (AggregateLog<X>) this;
			} else {
				throw new IllegalArgumentException();
			}
		}

		Version lastCommittedVersion();

		CommitResult<E> commit(Uncommitted<E> attempt);

		Stream<Committed<E>> commits();

		default Stream<E> events() {
			return eventsSince(Version.INITIAL);
		}

		default Stream<E> eventsSince(Version since) {
			return commits().filter(c -> c.after(since)).flatMap(Committed::events);
		}

		Identifier<E> getIdentifier();
	}

	interface EventLog<E> {

		Comparator<Committed<?>> ORDER_BY_TIMESTAMP = (c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp());

		default Stream<Committed<E>> commits() {
			return logs().flatMap(AggregateLog::commits).sorted(ORDER_BY_TIMESTAMP);
		}

		default Stream<E> events() {
			return commits().flatMap(Committed::events);
		}

		Class<E> getEventType();

		Stream<AggregateLog<E>> logs();

		AggregateLog<E> of(Identifier<E> identifier);
	}

	default <E> CommitResult<E> commit(Uncommitted<E> attempt) {
		return get(attempt.getIdentifier()).commit(attempt);
	}

	default <E> Function<Uncommitted<E>, CommitResult<E>> committer(EventBus bus) {
		return attempt -> commit(attempt).visitCommitted(bus::publish);
	}

	default <E> Stream<E> events(Identifier<E> identifier) {
		return get(identifier).events();
	}

	default <E> Stream<E> events(Identifier<E> identifier, Version since) {
		return get(identifier).eventsSince(since);
	}

	<E> EventLog<E> get(Class<E> eventType);

	default <E> AggregateLog<E> get(Identifier<E> identifier) {
		return get(identifier.getEventType()).of(identifier);
	}
}
