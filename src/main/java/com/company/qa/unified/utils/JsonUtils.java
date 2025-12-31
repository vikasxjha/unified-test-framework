package com.company.qa.unified.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * JSON utilities for test data and event handling.
 *
 * Usage:
 *   String json = "{\"key\": \"value\"}";
 *   Map<String, Object> map = JsonUtils.fromJson(json, Map.class);
 *   Object obj = JsonUtils.fromJson(json, MyClass.class);
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
        // utility
    }

    /**
     * Parse JSON string into the specified class type.
     *
     * @param json JSON string
     * @param clazz target class
     * @return deserialized object
     * @throws RuntimeException if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

    /**
     * Parse JSON string into a Map.
     *
     * @param json JSON string
     * @return map representation of JSON
     * @throws RuntimeException if parsing fails
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, Map.class);
    }

    /**
     * Convert object to JSON string.
     *
     * @param obj object to serialize
     * @return JSON string
     * @throws RuntimeException if serialization fails
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    /**
     * Pretty-print JSON string.
     *
     * @param json JSON string
     * @return formatted JSON
     * @throws RuntimeException if formatting fails
     */
    public static String prettyPrint(String json) {
        try {
            Object obj = MAPPER.readValue(json, Object.class);
            return MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to pretty-print JSON", e);
        }
    }

    /**
     * Alias for prettyPrint.
     */
    public static String pretty(String json) {
        return prettyPrint(json);
    }

    /**
     * Convert object to pretty JSON.
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to pretty JSON", e);
        }
    }

    /**
     * Read JSON value from string using JSON path.
     */
    public static String read(String json, String path) {
        try {
            Map<String, Object> map = toMap(json);
            Object value = map.get(path);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Count occurrences in JSON (simple implementation).
     */
    public static int count(String json, String field) {
        try {
            String fieldPattern = "\"" + field + "\"";
            int count = 0;
            int index = 0;
            while ((index = json.indexOf(fieldPattern, index)) != -1) {
                count++;
                index += fieldPattern.length();
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse InputStream to object.
     */
    public static <T> T fromJson(java.io.InputStream inputStream, Class<T> clazz) {
        try {
            return MAPPER.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON from InputStream", e);
        }
    }

    /**
     * Escape JSON string for embedding in another JSON string.
     * Useful for API requests that need JSON as a string value.
     *
     * @param json JSON string to escape
     * @return escaped JSON string suitable for use as JSON value
     */
    public static String escapeJson(String json) {
        if (json == null) {
            return "null";
        }
        try {
            // Use Jackson to properly escape the string
            return MAPPER.writeValueAsString(json);
        } catch (Exception e) {
            // Fallback to manual escaping
            return "\"" + json
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    + "\"";
        }
    }
}

