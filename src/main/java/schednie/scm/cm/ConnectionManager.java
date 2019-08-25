package schednie.scm.cm;


import schednie.scm.exception.NoAvailableConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection manager which allows switching and working with master
 * and slave ConnectionPool's.
 *
 * @author schednie
 */
public interface ConnectionManager {

	/**
	 * Tries first to retrieve Connection from master ConnectionPool. If it's unavailable -
	 * retrieves Connection from collection of slave ConnectionPool's.
	 *
	 * @return Connection from available ConnectionPool
	 * @throws NoAvailableConnectionPoolException if there is no available ConnectionPool within
	 *                                            this ConnectionManager
	 */
	Connection getConnection() throws NoAvailableConnectionPoolException;

	/**
	 * Give back a given Connection to appropriate ConnectionPool.
	 *
	 * @param connection Connection, initially retrieved from {@link #getConnection()}
	 * @return true - if given connection belongs to any ConnectionPool within this manager.
	 */
	boolean releaseConnection(Connection connection);


	/**
	 * Shutdown all underling ConnectionPool's
	 * @throws SQLException if a database access error occurs
	 */
	void shutdown() throws SQLException;
}
