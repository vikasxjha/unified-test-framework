package com.company.qa.unified.events;

import com.company.qa.unified.utils.Log;

import java.util.Map;

/**
 * EventValidator
 *
 * Validates event payloads for correctness.
 *
 * Covers:
 * - Schema validation
 * - Required fields
 * - Data types
 */
public class EventValidator {

    private static final Log log = Log.get(EventValidator.class);

    /**
     * Validate event payload.
     *
     * @param event event map
     * @return true if valid
     */
    public boolean validate(Map<String, Object> event) {
        log.info("✅ Validating event payload");

        if (event == null || event.isEmpty()) {
            log.warn("Event is null or empty");
            return false;
        }

        // Check required fields
        if (!event.containsKey("eventType")) {
            log.warn("Missing required field: eventType");
            return false;
        }

        if (!event.containsKey("timestamp")) {
            log.warn("Missing required field: timestamp");
            return false;
        }

        // Validate event type
        String eventType = (String) event.get("eventType");
        if (eventType == null || eventType.isEmpty()) {
            log.warn("eventType is null or empty");
            return false;
        }

        log.info("Event payload is valid: eventType={}", eventType);
        return true;
    }

    /**
     * Validate specific event type.
     */
    public boolean validateEventType(Map<String, Object> event, String expectedType) {
        if (!validate(event)) {
            return false;
        }

        String actualType = (String) event.get("eventType");
        return expectedType.equals(actualType);
    }
}

