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
}

