package ddd.aggregate;

import java.io.Serializable;

public class Version implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Version INITIAL = new Version(0);

	private final long value;

	public Version(long value) {
		this.value = value;
	}

	public boolean afterOrEqual(Version other) {
		return value >= other.value;
	}

	public boolean isInitial() {
		return value == 0;
	}

	public Version next(long value) {
		if (this.value < value) {
			return new Version(value);
		} else {
			throw new IllegalArgumentException("this version " + this.value + " is later than " + value);
		}
	}

	public Version next() {
		return new Version(value + 1);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		Version other = (Version) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}
}
