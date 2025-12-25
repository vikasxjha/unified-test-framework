package com.company.qa.unified.stepdefs;

import com.company.qa.unified.events.KafkaConsumerHelper;
import com.company.qa.unified.events.EventValidator;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EventSteps
 *
 * Cucumber step definitions for event validation (Kafka).
 *
 * Covers:
 * - Event consumption
 * - Event validation
 * - Message ordering
 * - Schema validation
 */
public class EventSteps {

    private static final Log log = Log.get(EventSteps.class);

    private KafkaConsumerHelper kafkaConsumer;
    private List<Map<String, Object>> capturedEvents;
    private String lastEventType;

    /* =========================================================
       SETUP
       ========================================================= */

    @Given("event consumption is enabled")
    public void enableEventConsumption() {
        log.info("📨 Enabling event consumption");

        kafkaConsumer = new KafkaConsumerHelper();
        kafkaConsumer.subscribeToTopics("lookup-events", "notification-events");

        ReportPublisher.step("Event consumption enabled");
    }

    /* =========================================================
       EVENT VALIDATION
       ========================================================= */

    @Then("a {string} should be published")
    public void verifyEventPublished(String eventType) {
        log.info("✅ Verifying event published: {}", eventType);

        assertNotNull(kafkaConsumer, "Kafka consumer not initialized");

        // Poll for events (wait up to 10 seconds)
        capturedEvents = kafkaConsumer.pollEvents(10000);

        assertNotNull(capturedEvents, "No events captured");
        assertFalse(capturedEvents.isEmpty(), "No events received");

        // Find event by type
        boolean found = capturedEvents.stream()
            .anyMatch(event -> eventType.equals(event.get("eventType")));

        assertTrue(
            found,
            "Event type '" + eventType + "' not found in captured events"
        );

        lastEventType = eventType;

        ReportPublisher.step("Event validated: " + eventType);
        ReportPublisher.attachJson("Captured Events", capturedEvents.toString());
    }

    @Then("the event should contain field {string}")
    public void verifyEventContainsField(String fieldName) {
        log.info("✅ Verifying event contains field: {}", fieldName);

        assertNotNull(capturedEvents, "No events captured");
        assertFalse(capturedEvents.isEmpty(), "No events to validate");

        Map<String, Object> event = capturedEvents.get(0);

        assertTrue(
            event.containsKey(fieldName),
            "Event does not contain field: " + fieldName
        );

        ReportPublisher.step("Event contains field: " + fieldName);
    }

    @Then("the event payload should be valid")
    public void verifyEventPayloadValid() {
        log.info("✅ Verifying event payload valid");

        assertNotNull(capturedEvents, "No events captured");
        assertFalse(capturedEvents.isEmpty(), "No events to validate");

        Map<String, Object> event = capturedEvents.get(0);

        // Validate using EventValidator
        EventValidator validator = new EventValidator();
        boolean isValid = validator.validate(event);

        assertTrue(isValid, "Event payload validation failed");

        ReportPublisher.step("Event payload is valid");
    }
}

