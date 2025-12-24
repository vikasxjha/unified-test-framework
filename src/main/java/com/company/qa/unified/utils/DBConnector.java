package com.company.qa.unified.utils;

import com.company.qa.unified.config.EnvironmentConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database connector for test data provisioning.
 *
 * Handles:
 * - Direct JDBC connections to test databases
 * - Parameterized queries to prevent SQL injection
 * - Connection pooling and cleanup
 *
 * NOTE: This is a simplified implementation for testing.
 * Production systems should use connection pools (HikariCP, etc).
 *
 * Usage:
 *   DBConnector.execute("INSERT INTO users ...", params);
 *   List<Map<String, Object>> rows = DBConnector.query("SELECT * FROM users");
 */
public final class DBConnector {

    private static final Log log = Log.get(DBConnector.class);

    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    private static final String DB_URL = System.getProperty(
            "db.url",
            getDefaultDbUrl()
    );

    private static final String DB_USER = System.getProperty(
            "db.user",
            "testuser"
    );

    private static final String DB_PASSWORD = System.getProperty(
            "db.password",
            "testpass"
    );

    private DBConnector() {
        // utility
    }

    /**
     * Get default database URL based on environment.
     */
    private static String getDefaultDbUrl() {
        try {
            EnvironmentConfig config = EnvironmentConfig.get();
            // Default to PostgreSQL on localhost for testing
            return "jdbc:postgresql://localhost:5432/testdb";
        } catch (Exception e) {
            log.warn("Failed to get env config, using default DB URL");
            return "jdbc:postgresql://localhost:5432/testdb";
        }
    }

    /**
     * Execute a SQL statement with parameters.
     * Auto-commits after execution.
     *
     * @param sql SQL query (supports ? placeholders)
     * @param params parameter values
     * @return number of rows affected
     */
    public static int execute(String sql, Object... params) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            int result = stmt.executeUpdate();
            conn.commit();

            log.debug("Executed SQL: {} | Rows affected: {}", sql, result);
            return result;

        } catch (SQLException e) {
            log.error("Failed to execute SQL: {}", sql, e);
            throw new RuntimeException("Database operation failed", e);
        }
    }

    /**
     * Execute a query and return all rows as Maps.
     *
     * @param sql SQL query
     * @param params parameter values
     * @return list of row maps
     */
    public static List<Map<String, Object>> query(String sql, Object... params) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> rows = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                int colCount = rs.getMetaData().getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    String colName = rs.getMetaData().getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(colName, value);
                }

                rows.add(row);
            }

            log.debug("Query returned {} rows", rows.size());
            return rows;

        } catch (SQLException e) {
            log.error("Failed to query: {}", sql, e);
            throw new RuntimeException("Database query failed", e);
        }
    }

    /**
     * Execute a query and return a single row.
     *
     * @param sql SQL query
     * @param params parameter values
     * @return first row as map, or null if no rows
     */
    public static Map<String, Object> queryOne(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Execute a query and return a single value.
     *
     * @param sql SQL query (should return single column)
     * @param params parameter values
     * @return first column of first row
     */
    public static Object queryValue(String sql, Object... params) {
        Map<String, Object> row = queryOne(sql, params);
        if (row == null || row.isEmpty()) {
            return null;
        }
        return row.values().iterator().next();
    }

    /**
     * Get or create a thread-local database connection.
     */
    private static Connection getConnection() throws SQLException {
        Connection conn = CONNECTION.get();
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            CONNECTION.set(conn);
            log.debug("Created new database connection");
        }
        return conn;
    }

    /**
     * Close and cleanup the thread-local connection.
     */
    public static void close() {
        try {
            Connection conn = CONNECTION.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                CONNECTION.remove();
                log.debug("Closed database connection");
            }
        } catch (SQLException e) {
            log.warn("Failed to close database connection", e);
        }
    }

    /**
     * Rollback the current transaction.
     */
    public static void rollback() {
        try {
            Connection conn = CONNECTION.get();
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                log.debug("Rolled back transaction");
            }
        } catch (SQLException e) {
            log.warn("Failed to rollback transaction", e);
        }
    }
}

