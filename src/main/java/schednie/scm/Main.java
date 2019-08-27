package schednie.scm;

import lombok.SneakyThrows;
import schednie.scm.cm.ConnectionManager;
import schednie.scm.cm.impl.SimpleConnectionManager;
import schednie.scm.cp.ConnectionPool;
import schednie.scm.cp.impl.SimpleConnectionPool;

import java.sql.Connection;
import java.util.Collections;

public class Main {

	private static final String H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
	private static final String POSTGRES_URL = "jdbc:postgresql://127.0.0.1:5432/test";
	private static final int POOL_SIZE = 10;

	private static final ConnectionPool masterCp = new SimpleConnectionPool(H2_URL, "", "", POOL_SIZE);
	private static final ConnectionPool slaveCp = new SimpleConnectionPool(POSTGRES_URL, "postgres", "coral", POOL_SIZE);
	private static final ConnectionManager scm = new SimpleConnectionManager(masterCp, Collections.singletonList(slaveCp));

	@SneakyThrows
	public static void main(String[] args) {
		System.out.println("===================================");
		Connection h2Conn = scm.getConnection();

		System.out.println(h2Conn.getMetaData().getDatabaseProductName());

		for (int i = 0; i < POOL_SIZE - 1; i++) {
			scm.getConnection();
		}

		// now postgres connection should be acquired, because of full h2 pool
		System.out.println("===================================");

		Connection postgresConn = scm.getConnection();

		System.out.println(postgresConn.getMetaData().getDatabaseProductName());

		scm.shutdown();
	}
}
