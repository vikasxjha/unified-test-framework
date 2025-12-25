package com.company.qa.unified.utils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DBAssertions
 *
 * Reusable database assertion utilities for E2E tests.
 *
 * Design goals:
 * - Explicit, readable assertions
 * - Clear failure messages
 * - No generic Object comparisons
 * - Thread-safe
 *
 * Usage:
 *   DBAssertions.assertRowExists("users", "email", "test@example.com");
 *   DBAssertions.assertColumnValue("users", "id", userId, "status", "ACTIVE");
 */
public final class DBAssertions {

    private static final Log log = Log.get(DBAssertions.class);

    private DBAssertions() {
        // utility class
    }

    /**
     * Assert that a row exists with specific column value.
     */
    public static void assertRowExists(
            String table,
            String columnName,
            Object columnValue
    ) {
        String sql = String.format(
                "SELECT COUNT(*) as count FROM %s WHERE %s = ?",
                table, columnName
        );

        Object result = DBConnector.queryValue(sql, columnValue);
        int count = ((Number) result).intValue();

        assertTrue(
                count > 0,
                String.format(
                        "Expected row in table '%s' with %s='%s' but none found",
                        table, columnName, columnValue
                )
        );

        log.info("✅ Verified row exists: {} where {}={}", table, columnName, columnValue);
    }

    /**
     * Assert that NO row exists with specific column value.
     */
    public static void assertRowNotExists(
            String table,
            String columnName,
            Object columnValue
    ) {
        String sql = String.format(
                "SELECT COUNT(*) as count FROM %s WHERE %s = ?",
                table, columnName
        );

        Object result = DBConnector.queryValue(sql, columnValue);
        int count = ((Number) result).intValue();

        assertEquals(
                0,
                count,
                String.format(
                        "Expected NO rows in table '%s' with %s='%s' but found %d",
                        table, columnName, columnValue, count
                )
        );

        log.info("✅ Verified row does NOT exist: {} where {}={}", table, columnName, columnValue);
    }

    /**
     * Assert specific column value for a row identified by ID.
     */
    public static void assertColumnValue(
            String table,
            String idColumn,
            Object idValue,
            String targetColumn,
            Object expectedValue
    ) {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                targetColumn, table, idColumn
        );

        Object actualValue = DBConnector.queryValue(sql, idValue);

        assertEquals(
                expectedValue,
                actualValue,
                String.format(
                        "Expected %s.%s='%s' for %s=%s, but got '%s'",
                        table, targetColumn, expectedValue, idColumn, idValue, actualValue
                )
        );

        log.info(
                "✅ Verified {}.{}={} where {}={}",
                table, targetColumn, expectedValue, idColumn, idValue
        );
    }

    /**
     * Assert column value matches one of expected values (for enums).
     */
    public static void assertColumnValueIn(
            String table,
            String idColumn,
            Object idValue,
            String targetColumn,
            Object... expectedValues
    ) {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                targetColumn, table, idColumn
        );

        Object actualValue = DBConnector.queryValue(sql, idValue);

        boolean found = false;
        for (Object expected : expectedValues) {
            if (expected.equals(actualValue)) {
                found = true;
                break;
            }
        }

        assertTrue(
                found,
                String.format(
                        "Expected %s.%s to be one of %s for %s=%s, but got '%s'",
                        table, targetColumn,
                        java.util.Arrays.toString(expectedValues),
                        idColumn, idValue, actualValue
                )
        );

        log.info(
                "✅ Verified {}.{}={} (one of expected values) where {}={}",
                table, targetColumn, actualValue, idColumn, idValue
        );
    }

    /**
     * Assert row count for a specific condition.
     */
    public static void assertRowCount(
            String table,
            String whereColumn,
            Object whereValue,
            int expectedCount
    ) {
        String sql = String.format(
                "SELECT COUNT(*) as count FROM %s WHERE %s = ?",
                table, whereColumn
        );

        Object result = DBConnector.queryValue(sql, whereValue);
        int actualCount = ((Number) result).intValue();

        assertEquals(
                expectedCount,
                actualCount,
                String.format(
                        "Expected %d rows in table '%s' where %s='%s', but found %d",
                        expectedCount, table, whereColumn, whereValue, actualCount
                )
        );

        log.info(
                "✅ Verified row count: {} rows in {} where {}={}",
                expectedCount, table, whereColumn, whereValue
        );
    }

    /**
     * Assert column value is NOT NULL.
     */
    public static void assertColumnNotNull(
            String table,
            String idColumn,
            Object idValue,
            String targetColumn
    ) {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                targetColumn, table, idColumn
        );

        Object value = DBConnector.queryValue(sql, idValue);

        assertNotNull(
                value,
                String.format(
                        "Expected %s.%s to be NOT NULL for %s=%s",
                        table, targetColumn, idColumn, idValue
                )
        );

        log.info(
                "✅ Verified {}.{} is NOT NULL where {}={}",
                table, targetColumn, idColumn, idValue
        );
    }

    /**
     * Assert column value IS NULL.
     */
    public static void assertColumnNull(
            String table,
            String idColumn,
            Object idValue,
            String targetColumn
    ) {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                targetColumn, table, idColumn
        );

        Object value = DBConnector.queryValue(sql, idValue);

        assertNull(
                value,
                String.format(
                        "Expected %s.%s to be NULL for %s=%s, but got '%s'",
                        table, targetColumn, idColumn, idValue, value
                )
        );

        log.info(
                "✅ Verified {}.{} is NULL where {}={}",
                table, targetColumn, idColumn, idValue
        );
    }

    /**
     * Assert numeric column value is greater than threshold.
     */
    public static void assertColumnGreaterThan(
            String table,
            String idColumn,
            Object idValue,
            String targetColumn,
            long threshold
    ) {
        String sql = String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                targetColumn, table, idColumn
        );

        Object result = DBConnector.queryValue(sql, idValue);
        long actualValue = ((Number) result).longValue();

        assertTrue(
                actualValue > threshold,
                String.format(
                        "Expected %s.%s > %d for %s=%s, but got %d",
                        table, targetColumn, threshold, idColumn, idValue, actualValue
                )
        );

        log.info(
                "✅ Verified {}.{}={} > {} where {}={}",
                table, targetColumn, actualValue, threshold, idColumn, idValue
        );
    }

    /**
     * Get a single row for custom assertions.
     */
    public static Map<String, Object> getRow(
            String table,
            String whereColumn,
            Object whereValue
    ) {
        String sql = String.format(
                "SELECT * FROM %s WHERE %s = ?",
                table, whereColumn
        );

        Map<String, Object> row = DBConnector.queryOne(sql, whereValue);

        assertNotNull(
                row,
                String.format(
                        "Expected row in table '%s' where %s='%s' but none found",
                        table, whereColumn, whereValue
                )
        );

        log.debug("Retrieved row from {} where {}={}", table, whereColumn, whereValue);
        return row;
    }

    /**
     * Delete test data by ID (cleanup).
     */
    public static void deleteTestData(
            String table,
            String idColumn,
            Object idValue
    ) {
        String sql = String.format(
                "DELETE FROM %s WHERE %s = ?",
                table, idColumn
        );

        int rowsDeleted = DBConnector.execute(sql, idValue);

        log.info(
                "🧹 Cleaned up test data: {} rows deleted from {} where {}={}",
                rowsDeleted, table, idColumn, idValue
        );
    }

    /**
     * Delete test data by column value (cleanup).
     */
    public static void deleteTestDataWhere(
            String table,
            String whereColumn,
            Object whereValue
    ) {
        String sql = String.format(
                "DELETE FROM %s WHERE %s = ?",
                table, whereColumn
        );

        int rowsDeleted = DBConnector.execute(sql, whereValue);

        log.info(
                "🧹 Cleaned up test data: {} rows deleted from {} where {}={}",
                rowsDeleted, table, whereColumn, whereValue
        );
    }
}

