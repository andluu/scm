package schednie.scm.cp.impl;

import lombok.extern.slf4j.Slf4j;
import schednie.scm.cp.ConnectionPool;
import schednie.scm.exception.BusyPoolException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Simple implementation of {@link ConnectionPool}. Uses {@link java.sql.DriverManager}.
 * This implementation is NOT thread safe!!
 * <p>
 *
 * @author schednie
 */
@Slf4j
public class SimpleConnectionPool implements ConnectionPool {

	private static final int DEFAULT_MAX_POOL_SIZE = 10;
	private final int maxPoolSize;

	private final Collection<Connection> connectionPool; // contains used and free connections
	private final Collection<Connection> usedConnections = new HashSet<>();

	private final String url;
	private final String user;
	private final String password;


	public SimpleConnectionPool(final String url, final String user,
								final String password, final int maxPoolSize) {

		this.url = url;
		this.user = user;
		this.password = password;
		this.maxPoolSize = maxPoolSize;

		this.connectionPool = new HashSet<>(this.maxPoolSize);
	}

	public SimpleConnectionPool(final String url, final String user,
								final String password) {

		this(url, user, password, DEFAULT_MAX_POOL_SIZE);
	}

	@Override
	public Connection getConnection() throws SQLException {

		if (connectionPool.size() == maxPoolSize)
			throw new BusyPoolException(maxPoolSize);

		if (containsFreeConnections()) {
			log.debug("Getting connection from connection pool");
			return connectionPool.stream().filter(conn -> !usedConnections.contains(conn)).findAny().get();
		}

		Connection newConn = createConnection(url, user, password);
		usedConnections.add(newConn);
		connectionPool.add(newConn);
		return newConn;
	}

	@Override
	public boolean releaseConnection(final Connection acquiredConnection) {

		log.debug("Releasing connection: {}", acquiredConnection);
		return usedConnections.remove(acquiredConnection);
	}

	@Override
	public String getUrl() {

		return url;
	}

	@Override
	public Collection<Connection> getConnectionPool() {

		return connectionPool;
	}

	@Override
	public void shutdown() throws SQLException {

		log.debug("Closing all connections {}", url);
		for (final Connection connection : connectionPool) {
			if (!connection.isClosed())
				connection.close();
		}
		connectionPool.clear();
		usedConnections.clear();
	}

	Collection<Connection> getUsedConnections() {

		return usedConnections;
	}

	private boolean containsFreeConnections() {

		return (connectionPool.size() - usedConnections.size()) > 0;
	}

	private static Connection createConnection(final String url, final String user,
											   final String password) throws SQLException {

		log.debug("Creating new connection url: {}, user: {}", url, user);
		return DriverManager.getConnection(url, user, password);
	}
}
