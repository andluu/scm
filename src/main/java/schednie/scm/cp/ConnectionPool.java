package schednie.scm.cp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Pool of {@link java.sql.Connection} over specific database.
 *
 * @author schednie
 */
public interface ConnectionPool {

	/**
	 * Creates new Connection.
	 * @return new Connection.
	 * @throws SQLException if a database access error occurs or the url is {@code null}
	 */
	Connection getConnection() throws SQLException;

	/**
	 * Releases used connection.
	 * @param connection used connection
	 * @return true - if given connection belongs to this ConnectionPool
	 */
	boolean releaseConnection(Connection connection);

	/**
	 * Returns url of the underling database.
	 * @return url of the database.
	 */
	String getUrl();

	/**
	 * Returns collection of Connection pool.
	 * @return collection of Connection pool
	 */
	Collection<Connection> getConnectionPool();

	/**
	 * Closing all connection in Connection pool
	 * @throws SQLException if a database access error occurs
	 */
	void shutdown() throws SQLException;
}
