package ddd.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ddd.aggregate.Recorded.CommitResult;
import ddd.aggregate.Recorded.Committed;
import ddd.aggregate.Recorded.Uncommitted;

public class AggregateRoot<T, E> {

	private final Identifier<E> identifier;
	private final EventHandler<T> handler;
	private final List<E> changes;
	private Version expected;
	private T state;

	public AggregateRoot(Identifier<E> identifier, EventHandler<T> handler) {
		this(identifier, handler, Version.INITIAL, null);
	}

	public AggregateRoot(Identifier<E> identifier, EventHandler<T> handler, Version expected, T state) {
		this.identifier = Objects.requireNonNull(identifier);
		this.expected = Objects.requireNonNull(expected);
		this.handler = Objects.requireNonNull(handler);
		this.changes = new ArrayList<>();
		this.state = state;
	}

	public AggregateRoot<T, E> perform(Function<? super T, ? extends E> command) {
		return record(command.apply(state));
	}

	public Identifier<E> getIdentifier() {
		return identifier;
	}

	public Version getExpected() {
		return expected;
	}

	public AggregateRoot<T, E> applyEvent(E event) {
		state = handler.apply(state, event);
		return this;
	}

	public AggregateRoot<T, E> record(E event) {
		Objects.requireNonNull(event);
		applyEvent(event);
		changes.add(event);
		return this;
	}

	private void onCommit(Committed<E> committed) {
		changes.clear();
		expected = committed.getVersion();
	}

	public void tryCommit(Function<Uncommitted<E>, CommitResult<E>> committer) {
		committer.apply(new Uncommitted<>(identifier, expected, changes)).visitCommitted(this::onCommit);
	}

	public void updateFromHistory(BiFunction<Identifier<E>, Version, Stream<E>> eventSupplier) {
		updateFromHistory(eventSupplier.apply(identifier, expected));
	}

	public void updateFromHistory(Stream<E> events) {
		events.forEach(this::applyEvent);
	}

	public AggregateRoot<T, E> testState(Predicate<? super T> check) {
		if (check.test(state)) {
			return this;
		} else {
			throw new AssertionError(state);
		}
	}

	public AggregateRoot<T, E> testChangesContain(E event) {
		if (changes.contains(event)) {
			return this;
		} else {
			throw new AssertionError(changes + " does not contain " + event);
		}
	}

	public AggregateRoot<T, E> testChangesNotContain(E event) {
		if (!changes.contains(event)) {
			return this;
		} else {
			throw new AssertionError(changes + " does contain " + event);
		}
	}

	public AggregateRoot<T, E> testVersion(Version version) {
		if (expected.equals(version)) {
			return this;
		} else {
			throw new AssertionError(version + " does not match " + expected);
		}
	}
}