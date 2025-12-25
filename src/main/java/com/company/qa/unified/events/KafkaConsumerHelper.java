package com.company.qa.unified.events;

import com.company.qa.unified.utils.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KafkaConsumerHelper
 *
 * Simplified wrapper around KafkaTestConsumer for step definitions.
 *
 * Provides:
 * - Easy topic subscription
 * - Event polling with timeout
 * - JSON parsing
 */
public class KafkaConsumerHelper {

    private static final Log log = Log.get(KafkaConsumerHelper.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private KafkaTestConsumer consumer;
    private List<String> subscribedTopics;

    public KafkaConsumerHelper() {
        this.subscribedTopics = new ArrayList<>();
        this.consumer = KafkaTestConsumer.create("test-consumer-" + System.currentTimeMillis());
    }

    /**
     * Subscribe to multiple topics.
     */
    public void subscribeToTopics(String... topics) {
        log.info("📡 Subscribing to topics: {}", String.join(", ", topics));

        for (String topic : topics) {
            consumer.subscribe(topic);
            subscribedTopics.add(topic);
        }
    }

    /**
     * Poll for events with timeout in milliseconds.
     *
     * @param timeoutMillis timeout in milliseconds
     * @return list of parsed events as maps
     */
    public List<Map<String, Object>> pollEvents(long timeoutMillis) {
        log.info("⏳ Polling events (timeout={}ms)", timeoutMillis);

        List<Map<String, Object>> events = new ArrayList<>();

        try {
            Duration timeout = Duration.ofMillis(timeoutMillis);
            List<ConsumerRecord<String, String>> records = consumer.pollUntilAny(timeout);

            for (ConsumerRecord<String, String> record : records) {
                try {
                    Map<String, Object> event = parseEvent(record);
                    events.add(event);

                    log.debug("📨 Event received: topic={}, key={}",
                        record.topic(), record.key());
                } catch (Exception e) {
                    log.warn("Failed to parse event: {}", e.getMessage());

                    // Add raw event
                    Map<String, Object> rawEvent = new HashMap<>();
                    rawEvent.put("topic", record.topic());
                    rawEvent.put("key", record.key());
                    rawEvent.put("value", record.value());
                    rawEvent.put("parseError", e.getMessage());
                    events.add(rawEvent);
                }
            }

            log.info("✅ Polled {} events", events.size());

        } catch (Exception e) {
            log.error("Failed to poll events", e);
        }

        return events;
    }

    /**
     * Parse event JSON to Map.
     */
    private Map<String, Object> parseEvent(ConsumerRecord<String, String> record) {
        Map<String, Object> event = new HashMap<>();

        event.put("topic", record.topic());
        event.put("key", record.key());
        event.put("offset", record.offset());
        event.put("partition", record.partition());
        event.put("timestamp", record.timestamp());

        // Parse JSON value
        String value = record.value();
        if (value != null && !value.isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = JSON.readValue(value, Map.class);
                event.putAll(payload);
            } catch (Exception e) {
                // Not JSON, store as raw string
                event.put("rawValue", value);
            }
        }

        return event;
    }

    /**
     * Close consumer.
     */
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}

