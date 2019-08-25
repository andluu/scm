package schednie.scm.cp.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import schednie.scm.exception.BusyPoolException;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SimpleConnectionPoolTests {

	private static final int POOL_SIZE = 10;
	private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

	private SimpleConnectionPool scp = new SimpleConnectionPool(JDBC_URL, "", "", POOL_SIZE);

	@Before
	public void setUp() throws Exception {
		Class.forName("org.h2.Driver");
	}

	@After
	public void tearDown() throws Exception {
		Collection<Connection> connectionPool = scp.getConnectionPool();
		for (Connection connection : connectionPool) {
			connection.close();
		}
	}

	@Test
	public void getConnection_usedConnectionsContainsAcquired() throws Exception {
		Connection acquiredConn = scp.getConnection();

		assertTrue(scp.getUsedConnections().contains(acquiredConn));
	}

	@Test
	public void getConnection_connectionPoolContainsAcquired() throws Exception {
		Connection acquiredConn = scp.getConnection();

		assertTrue(scp.getConnectionPool().contains(acquiredConn));
	}

	@Test(expected = BusyPoolException.class)
	public void getConnection_fullPool_throws() throws Exception {
		for (int i = 0; i < POOL_SIZE; i++) // fill pool
			scp.getConnection();

		scp.getConnection(); // throws
	}

	@Test
	public void releaseConnection_usedConnectionsNotContainReleased() throws Exception {
		Connection releasedConn = scp.getConnection();

		scp.releaseConnection(releasedConn);

		assertFalse(scp.getUsedConnections().contains(releasedConn));
	}

	@Test
	public void releaseConnection_releaseAcquired_true() throws Exception {
		Connection releasedConn = scp.getConnection();

		boolean released = scp.releaseConnection(releasedConn);

		assertTrue(released);
	}

	@Test
	public void releaseConnection_releaseAlien_false() throws Exception {
		Connection connMock = mock(Connection.class);

		boolean released = scp.releaseConnection(connMock);

		assertFalse(released);
	}

	@Test
	public void getUrl() {
		assertEquals(JDBC_URL, scp.getUrl());
	}

	@Test
	public void getConnectionPool_afterCreation_isEmpty() {
		assertTrue(scp.getConnectionPool().isEmpty());
	}

	@Test
	public void shutdown_allPoolClosed() throws Exception {
		initPoolWithTestConnections();
		Collection<Connection> connectionPool = scp.getConnectionPool();

		scp.shutdown();

		assertTrue(connectionPool.stream().allMatch(connection -> {
			try {
				return connection.isClosed();
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}));
	}

	@Test
	public void shutdown_connectionPoolAndUsedEmpty() throws Exception {
		initPoolWithTestConnections();
		scp.shutdown();

		assertTrue(scp.getConnectionPool().isEmpty());
		assertTrue(scp.getUsedConnections().isEmpty());
	}

	private void initPoolWithTestConnections() throws Exception {
		Collection<Connection> connections = new HashSet<>();
		for (int i = 0; i < POOL_SIZE; i++)
			connections.add(scp.getConnection());

		for (int i = 0; i < POOL_SIZE / 2; i++) {
			scp.releaseConnection(connections.stream().findAny().get());
		}
	}
}