package schednie.scm.exception;

public class BusyPoolException extends RuntimeException {

	private final int maxPoolSize;

	public BusyPoolException(final int maxPoolSize) {
		super("There is no free connections: " + maxPoolSize);
		this.maxPoolSize = maxPoolSize;
	}

	public BusyPoolException(final Throwable cause, final int maxPoolSize) {
		super("There is no free connections: " + maxPoolSize, cause);
		this.maxPoolSize = maxPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}
}
