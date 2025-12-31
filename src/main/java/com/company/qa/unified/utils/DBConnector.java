package com.company.qa.unified.utils;

import com.company.qa.unified.config.EnvironmentConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database connector with HikariCP connection pooling for test data provisioning.
 *
 * Features:
 * - Production-grade connection pooling via HikariCP
 * - Automatic connection validation and health checks
 * - Parameterized queries to prevent SQL injection
 * - Thread-safe connection management
 * - Configurable pool size and connection timeouts
 *
 * Configuration via system properties:
 * - db.url: JDBC connection URL
 * - db.user: Database username
 * - db.password: Database password
 * - db.pool.size: Maximum pool size (default: 10)
 * - db.pool.timeout: Connection timeout in ms (default: 30000)
 *
 * Usage:
 *   DBConnector.execute("INSERT INTO users (email, name) VALUES (?, ?)", email, name);
 *   List<Map<String, Object>> rows = DBConnector.query("SELECT * FROM users WHERE email = ?", email);
 *   Map<String, Object> user = DBConnector.queryOne("SELECT * FROM users WHERE id = ?", userId);
 *   Integer count = (Integer) DBConnector.queryValue("SELECT COUNT(*) FROM users");
 */
public final class DBConnector {

    private static final Log log = Log.get(DBConnector.class);

    private static HikariDataSource dataSource;

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

    private static final int POOL_SIZE = Integer.parseInt(
            System.getProperty("db.pool.size", "10")
    );

    private static final int CONNECTION_TIMEOUT = Integer.parseInt(
            System.getProperty("db.pool.timeout", "30000")
    );

    static {
        initializeConnectionPool();
    }

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
     * Initialize HikariCP connection pool.
     */
    private static void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);

            // Connection pool settings
            config.setMaximumPoolSize(POOL_SIZE);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes

            // Performance optimizations
            config.setAutoCommit(false);
            config.setConnectionTestQuery("SELECT 1");

            // Pool name for monitoring
            config.setPoolName("TestFrameworkPool");

            // Connection validation
            config.setValidationTimeout(5000);
            config.setLeakDetectionThreshold(60000); // 1 minute

            dataSource = new HikariDataSource(config);

            log.info("HikariCP connection pool initialized: URL={}, MaxPoolSize={}", DB_URL, POOL_SIZE);

        } catch (Exception e) {
            log.error("Failed to initialize HikariCP connection pool", e);
            throw new RuntimeException("Database connection pool initialization failed", e);
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
     * Shutdown the HikariCP connection pool.
     * Should be called during application shutdown or test cleanup.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP connection pool closed");
        }
    }

    /**
     * Get pool statistics for monitoring.
     *
     * @return map containing pool metrics
     */
    public static Map<String, Object> getPoolStats() {
        if (dataSource == null) {
            return Map.of("status", "not_initialized");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("active", dataSource.getHikariPoolMXBean().getActiveConnections());
        stats.put("idle", dataSource.getHikariPoolMXBean().getIdleConnections());
        stats.put("total", dataSource.getHikariPoolMXBean().getTotalConnections());
        stats.put("waiting", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        return stats;
    }
}

