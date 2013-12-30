package dbsearch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * dbsearch: Db
 * Created by dboitnot on 12/29/13.
 */
public class Db {
    private Connection conn;

    public Db(String url, String username, String password) throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception ex) {
            // Should never happen.
            throw new RuntimeException(ex);
        }

        conn = DriverManager.getConnection(url, username, password);
    }

    public void close() throws SQLException {
        if (conn != null)
            conn.close();
    }

    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }
}
