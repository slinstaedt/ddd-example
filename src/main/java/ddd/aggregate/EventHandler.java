package ddd.aggregate;

import java.util.function.BiConsumer;
import java.util.function.Function;

@FunctionalInterface
public interface EventHandler<T> {

	static <T> EventHandler<T> ignoreUnhandled() {
		return (t, e) -> {
			return t;
		};
	}
	
	static <T> EventHandler<T> unhandled() {
		return (t, e) -> {
			throw new IllegalArgumentException("event " + e + " does not apply to " + t);
		};
	}

	T apply(T target, Object event);

	default <E> EventHandler<T> when(Class<E> eventType, Function<? super E, ? extends T> factory) {
		return (t, e) -> eventType.isInstance(e) && t == null ? factory.apply(eventType.cast(e)) : apply(t, e);
	}

	default <E> EventHandler<T> when(Class<E> eventType, BiConsumer<? super T, ? super E> handler) {
		return (t, e) -> {
			if (eventType.isInstance(e) && t != null) {
				handler.accept(t, eventType.cast(e));
				return t;
			} else {
				return apply(t, e);
			}
		};
	}

}
