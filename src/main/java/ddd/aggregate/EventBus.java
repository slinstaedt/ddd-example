package ddd.aggregate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import ddd.aggregate.Recorded.Committed;

public interface EventBus {

	@FunctionalInterface
	interface EventListener<T> {

		void apply(Committed<T> committed);
	}

	class Memory implements EventBus {

		private static class Listener<T> {

			private final Class<T> eventType;
			private final EventListener<T> listener;

			public Listener(Class<T> eventType, EventListener<T> listener) {
				this.eventType = Objects.requireNonNull(eventType);
				this.listener = Objects.requireNonNull(listener);
			}

			void notify(Committed<?> committed) {
				committed.asOf(eventType).ifPresent(listener::apply);
			}
		}

		private final List<Listener<?>> listeners;

		public Memory() {
			listeners = new CopyOnWriteArrayList<>();
		}

		@Override
		public <T> void registerCommitListener(Class<T> eventType, EventListener<T> listener) {
			listeners.add(new Listener<>(eventType, listener));
		}

		@Override
		public void publish(Committed<?> committed) {
			listeners.forEach(l -> l.notify(committed));
		}
	}

	<T> void registerCommitListener(Class<T> eventType, EventListener<T> listener);

	void publish(Committed<?> committed);

	default <T> void registerEventListener(Class<T> eventType, Consumer<T> listener) {
		registerCommitListener(eventType, c -> c.events().forEach(listener));
	}
}
