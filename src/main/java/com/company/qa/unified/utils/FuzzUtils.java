package com.company.qa.unified.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/**
 * FuzzUtils
 *
 * Utility class for fuzz / negative / robustness testing.
 *
 * Used for:
 * - API fuzz testing
 * - Security testing
 * - Input validation testing
 * - Contract hardening
 *
 * RULES:
 * ❌ Must NOT throw unless explicitly requested
 * ❌ Must NOT depend on environment
 * ✅ Deterministic randomness when seeded
 */
public final class FuzzUtils {

    private static final Log log =
            Log.get(FuzzUtils.class);

    private static final String ALPHANUM =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final String SPECIAL_CHARS =
            "!@#$%^&*()_+-=[]{}|;:'\",.<>/?`~";

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private FuzzUtils() {
        // Utility class
    }

    /* =========================================================
       STRING FUZZING
       ========================================================= */

    /**
     * Random alphanumeric string.
     */
    public static String randomAlphaNumeric(int length) {
        return randomFromCharset(ALPHANUM, length);
    }

    /**
     * Random string including special characters.
     */
    public static String randomWithSpecialChars(int length) {
        return randomFromCharset(ALPHANUM + SPECIAL_CHARS, length);
    }

    /**
     * Extremely long string (boundary testing).
     */
    public static String longString(int length) {
        log.debug("Generating long string of length {}", length);
        return randomFromCharset(ALPHANUM, length);
    }

    /**
     * SQL injection style payloads.
     */
    public static List<String> sqlInjectionPayloads() {
        return List.of(
                "' OR '1'='1",
                "'; DROP TABLE users; --",
                "\" OR \"\" = \"",
                "admin'--",
                "' OR 1=1 --"
        );
    }

    /**
     * XSS payloads.
     */
    public static List<String> xssPayloads() {
        return List.of(
                "<script>alert('xss')</script>",
                "<img src=x onerror=alert(1)>",
                "<svg/onload=alert(1)>",
                "<iframe src=javascript:alert(1)>"
        );
    }

    /* =========================================================
       NUMBER FUZZING
       ========================================================= */

    public static List<Integer> boundaryIntegers() {
        return List.of(
                Integer.MIN_VALUE,
                -1,
                0,
                1,
                Integer.MAX_VALUE
        );
    }

    public static List<Long> boundaryLongs() {
        return List.of(
                Long.MIN_VALUE,
                -1L,
                0L,
                1L,
                Long.MAX_VALUE
        );
    }

    /* =========================================================
       JSON / STRUCTURE FUZZING
       ========================================================= */

    /**
     * Removes required fields.
     */
    public static Map<String, Object> removeField(
            Map<String, Object> original,
            String field
    ) {
        Map<String, Object> copy = new HashMap<>(original);
        copy.remove(field);
        return copy;
    }

    /**
     * Replace field with invalid value.
     */
    public static Map<String, Object> replaceField(
            Map<String, Object> original,
            String field,
            Object value
    ) {
        Map<String, Object> copy = new HashMap<>(original);
        copy.put(field, value);
        return copy;
    }

    /**
     * Randomly corrupt JSON values.
     */
    public static Map<String, Object> corruptRandomField(
            Map<String, Object> original
    ) {

        if (original.isEmpty()) {
            return original;
        }

        Map<String, Object> copy =
                new HashMap<>(original);

        List<String> keys =
                new ArrayList<>(copy.keySet());

        String key =
                keys.get(RANDOM.nextInt(keys.size()));

        copy.put(key, randomWithSpecialChars(50));

        log.debug("Corrupted field {}", key);
        return copy;
    }

    /* =========================================================
       ENUM / PARAMETER FUZZING
       ========================================================= */

    public static <T> T randomEnum(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        return constants[RANDOM.nextInt(constants.length)];
    }

    public static List<String> invalidEnumValues() {
        return List.of(
                "",
                "INVALID",
                "123",
                randomAlphaNumeric(10)
        );
    }

    /* =========================================================
       TIMESTAMP / DATE FUZZING
       ========================================================= */

    public static List<String> invalidTimestamps() {
        return List.of(
                "not-a-date",
                "9999-99-99T99:99:99Z",
                "-100000",
                Instant.now().plusSeconds(999999999).toString()
        );
    }

    /* =========================================================
       GENERIC FUZZ EXECUTOR
       ========================================================= */

    /**
     * Apply fuzz inputs to a function safely.
     */
    public static <T, R> void fuzzExecute(
            Collection<T> inputs,
            Function<T, R> executor
    ) {

        for (T input : inputs) {
            try {
                executor.apply(input);
            } catch (Exception e) {
                log.warn("Fuzz execution failed for input={}", input, e);
            }
        }
    }

    /* =========================================================
       INTERNAL
       ========================================================= */

    private static String randomFromCharset(
            String charset,
            int length
    ) {

        StringBuilder sb =
                new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(
                    charset.charAt(
                            RANDOM.nextInt(charset.length())
                    )
            );
        }
        return sb.toString();
    }
}
