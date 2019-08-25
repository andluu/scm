package schednie.scm.exception;

import schednie.scm.cp.ConnectionPool;

import java.util.Collection;

public class NoAvailableConnectionPoolException extends RuntimeException {

	private final Collection<ConnectionPool> connectionPools;


	public NoAvailableConnectionPoolException(final Collection<ConnectionPool> connectionPools) {
		super("There is no free connection pools: " + connectionPools);
		this.connectionPools = connectionPools;
	}

	public NoAvailableConnectionPoolException(final Throwable cause,
											  final Collection<ConnectionPool> connectionPools) {
		super("There is no free connection pools: " + connectionPools, cause);
		this.connectionPools = connectionPools;
	}

	public Collection<ConnectionPool> getConnectionPools() {
		return connectionPools;
	}
}
