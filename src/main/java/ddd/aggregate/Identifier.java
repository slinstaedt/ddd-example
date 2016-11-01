package ddd.aggregate;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Identifier<E> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static <E> Identifier<E> create(Class<E> eventType) {
		return new Identifier<>(eventType, UUID.randomUUID());
	}

	public static <E> Identifier<E> fromString(String value) {
		String[] split = value.split(":");
		return fromValue(split[0], split[1]);
	}

	public static <E> Identifier<E> fromValue(String eventType, String value) {
		try {
			@SuppressWarnings("unchecked")
			Class<E> type = (Class<E>) Class.forName(eventType);
			UUID uuid = UUID.fromString(value);
			return new Identifier<>(type, uuid);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private final Class<E> eventType;
	private final UUID value;

	private Identifier(Class<E> eventType, UUID value) {
		this.eventType = Objects.requireNonNull(eventType);
		this.value = Objects.requireNonNull(value);
	}

	public <X> Optional<Identifier<X>> asOf(Class<X> type) {
		if (eventType.isAssignableFrom(type)) {
			return Optional.of(new Identifier<>(type, value));
		} else {
			return Optional.empty();
		}
	}

	public Class<E> getEventType() {
		return eventType;
	}

	public String value() {
		return value.toString();
	}

	@Override
	public String toString() {
		return eventType.getCanonicalName() + ":" + value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Identifier<?> other = (Identifier<?>) obj;
		if (eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!eventType.equals(other.eventType)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
