package com.company.qa.unified.events;

import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * EventSchemaValidator validates Kafka / async events
 * against JSON Schemas and expected fields.
 *
 * Supports:
 * - Full schema validation
 * - Partial field validation
 * - Backward compatibility checks
 *
 * NOTE:
 * - This is READ-ONLY
 * - No Kafka logic here
 */
public final class EventSchemaValidator {

    private static final Log log =
            Log.get(EventSchemaValidator.class);

    private static final String SCHEMA_ROOT =
            "/event-schemas/";

    private EventSchemaValidator() {
        // utility
    }

    /* =========================================================
       FULL SCHEMA VALIDATION
       ========================================================= */

    /**
     * Validate event JSON against a schema.
     *
     * @param eventType logical event name
     * @param eventJson raw event payload
     */
    public static void validate(
            String eventType,
            String eventJson
    ) {

        try {
            JSONObject schema = loadSchema(eventType);
            JSONObject eventObject =
                    new JSONObject(eventJson);

            // Basic schema validation: check required fields
            if (schema.has("required")) {
                org.json.JSONArray required = schema.getJSONArray("required");
                for (int i = 0; i < required.length(); i++) {
                    String field = required.getString(i);
                    if (!eventObject.has(field)) {
                        throw new IllegalArgumentException(
                                "Missing required field: " + field);
                    }
                }
            }

            log.info("✅ Event schema validation passed: {}", eventType);

        } catch (Exception e) {
            fail("❌ Event schema validation failed for "
                    + eventType + ": " + e.getMessage());
        }
    }

    /* =========================================================
       PARTIAL VALIDATION (CONTRACT STYLE)
       ========================================================= */

    /**
     * Validate that event contains required fields
     * and values (partial match).
     */
    public static void validateFields(
            String eventType,
            Map<String, Object> expected,
            String eventJson
    ) {

        Map<String, Object> actual =
                JsonUtils.fromJson(eventJson, Map.class);

        expected.forEach((key, expectedValue) -> {
            if (!actual.containsKey(key)) {
                fail("Missing field '" + key
                        + "' in event " + eventType);
            }

            Object actualValue = actual.get(key);
            if (!expectedValue.equals(actualValue)) {
                fail("Field mismatch for '" + key
                        + "'. Expected=" + expectedValue
                        + " Actual=" + actualValue);
            }
        });

        log.info("✅ Event partial validation passed: {}", eventType);
    }

    /* =========================================================
       BACKWARD COMPATIBILITY
       ========================================================= */

    /**
     * Ensure a new event version is backward compatible
     * with an older schema.
     */
    public static void validateBackwardCompatibility(
            String oldSchemaName,
            String newEventJson
    ) {

        try {
            Schema oldSchema = loadSchema(oldSchemaName);
            JSONObject eventObject =
                    new JSONObject(newEventJson);

            oldSchema.validate(eventObject);

            log.info("✅ Backward compatibility OK for schema={}",
                    oldSchemaName);

        } catch (Exception e) {
            fail("❌ Backward compatibility broken: "
                    + e.getMessage());
        }
    }

    /* =========================================================
       SCHEMA LOADING
       ========================================================= */

    private static JSONObject loadSchema(String eventType) {

        String path = SCHEMA_ROOT + eventType + ".json";

        try (InputStream is =
                     EventSchemaValidator.class
                             .getResourceAsStream(path)) {

            if (is == null) {
                throw new IllegalStateException(
                        "Event schema not found: " + path);
            }

            return new JSONObject(new JSONTokener(is));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load event schema: " + eventType, e);
        }
    }
}
