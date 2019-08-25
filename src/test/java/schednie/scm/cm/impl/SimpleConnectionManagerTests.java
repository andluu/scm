package schednie.scm.cm.impl;

import org.junit.Before;
import org.junit.Test;
import schednie.scm.cp.ConnectionPool;
import schednie.scm.exception.NoAvailableConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SimpleConnectionManagerTests {

	private SimpleConnectionManager scm;
	private ConnectionPool masterCpMock = mock(ConnectionPool.class);
	private ConnectionPool slaveCpMock = mock(ConnectionPool.class);


	@Before
	public void setUp() {
		scm = new SimpleConnectionManager(masterCpMock, Collections.singleton(slaveCpMock));
	}

	@Test
	public void getConnection_masterAndSlaveAvailable_returnMastersConnection() throws SQLException {
		Connection mastersConnMock = mock(Connection.class);
		when(masterCpMock.getConnection()).thenReturn(mastersConnMock);
		when(slaveCpMock.getConnection()).thenReturn(mock(Connection.class));

		Connection conn = scm.getConnection();

		assertEquals(mastersConnMock, conn);
	}

	@Test
	public void getConnection_onlyMasterAvailable_returnMastersConnection() throws SQLException {
		Connection mastersConnMock = mock(Connection.class);
		when(masterCpMock.getConnection()).thenReturn(mastersConnMock);
		when(slaveCpMock.getConnection()).thenThrow(new SQLException());

		Connection conn = scm.getConnection();

		assertEquals(mastersConnMock, conn);
	}

	@Test
	public void getConnection_onlySlaveAvailable_returnSlavesConnection() throws SQLException {
		Connection slavesConnMock = mock(Connection.class);
		when(masterCpMock.getConnection()).thenThrow(new SQLException());
		when(slaveCpMock.getConnection()).thenReturn(slavesConnMock);

		Connection conn = scm.getConnection();

		assertEquals(slavesConnMock, conn);
	}

	@Test(expected = NoAvailableConnectionPoolException.class)
	public void getConnection_nonAvailable_throws() throws SQLException {
		when(masterCpMock.getConnection()).thenThrow(new SQLException());
		when(slaveCpMock.getConnection()).thenThrow(new SQLException());

		scm.getConnection();
	}


	@Test
	public void releaseConnection_releaseEach() {
		Connection connMock = mock(Connection.class);

		scm.releaseConnection(connMock);

		verify(masterCpMock, times(1)).releaseConnection(connMock);
		verify(slaveCpMock, times(1)).releaseConnection(connMock);
	}

	@Test
	public void releaseConnection_masterTrue_returnTrue() {
		Connection connMock = mock(Connection.class);
		when(masterCpMock.releaseConnection(connMock)).thenReturn(true);
		when(slaveCpMock.releaseConnection(connMock)).thenReturn(false);

		boolean released = scm.releaseConnection(connMock);

		assertTrue(released);
	}

	@Test
	public void releaseConnection_slaveTrue_returnTrue() {
		Connection connMock = mock(Connection.class);
		when(masterCpMock.releaseConnection(connMock)).thenReturn(false);
		when(slaveCpMock.releaseConnection(connMock)).thenReturn(true);

		boolean released = scm.releaseConnection(connMock);

		assertTrue(released);
	}

	@Test
	public void releaseConnection_bothFalse_returnFalse() {
		Connection connMock = mock(Connection.class);
		when(masterCpMock.releaseConnection(connMock)).thenReturn(false);
		when(slaveCpMock.releaseConnection(connMock)).thenReturn(false);

		boolean released = scm.releaseConnection(connMock);

		assertFalse(released);
	}

	@Test
	public void shutdown_shutdownAll() throws SQLException {
		scm.shutdown();

		verify(masterCpMock, times(1)).shutdown();
		verify(slaveCpMock, times(1)).shutdown();
	}


}