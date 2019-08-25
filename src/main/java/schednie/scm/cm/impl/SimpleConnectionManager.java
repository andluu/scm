package schednie.scm.cm.impl;


import lombok.extern.slf4j.Slf4j;
import schednie.scm.cm.ConnectionManager;
import schednie.scm.cp.ConnectionPool;
import schednie.scm.exception.NoAvailableConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Simple implementation of {@link ConnectionManager}.
 * This implementation is NOT thread safe!!
 *
 * @author schednie
 */
@Slf4j
public class SimpleConnectionManager implements ConnectionManager {

	private final ConnectionPool masterConnectionPool;
	private final Collection<ConnectionPool> slaveConnectionPools;


	public SimpleConnectionManager(final ConnectionPool masterConnectionPool,
								   final Collection<ConnectionPool> slaveConnectionPools) {

		this.masterConnectionPool = masterConnectionPool;
		this.slaveConnectionPools = slaveConnectionPools;
	}

	@Override
	public Connection getConnection() {

		// first try to retrieve from master
		Connection masterConn = tryGetConnection(masterConnectionPool);
		if (masterConn != null) return masterConn;

		// then slaves
		for (ConnectionPool slaveConnectionPool : slaveConnectionPools) {
			Connection slaveConn = tryGetConnection(slaveConnectionPool);
			if (slaveConn != null) return slaveConn;
		}

		Collection<ConnectionPool> busyPools = new HashSet<>(slaveConnectionPools);
		busyPools.add(masterConnectionPool);
		throw new NoAvailableConnectionPoolException(busyPools);
	}

	@Override
	public boolean releaseConnection(final Connection connection) {

		log.debug("Releasing connection: {}", connection);
		if (masterConnectionPool.releaseConnection(connection)) return true;
		return slaveConnectionPools.stream()
				.anyMatch(connectionPool -> connectionPool.releaseConnection(connection));
	}

	@Override
	public void shutdown() throws SQLException {

		masterConnectionPool.shutdown();
		for (final ConnectionPool slaveConnectionPool : slaveConnectionPools)
			slaveConnectionPool.shutdown();
	}

	private Connection tryGetConnection(ConnectionPool connectionPool) {

		try {
			return connectionPool.getConnection();
		}
		catch (Exception e) {
			log.warn("Connection pool is unavailable: " + connectionPool.getUrl(), e);
			return null;
		}
	}


}
