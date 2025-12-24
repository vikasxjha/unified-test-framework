package com.company.qa.unified.events;

import com.company.qa.unified.drivers.KafkaClientFactory;
import com.company.qa.unified.utils.Log;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * High-level Kafka consumer utility for tests.
 *
 * Features:
 * - Deterministic polling with timeout
 * - Predicate-based filtering
 * - Topic subscription management
 * - Clean failure messages
 *
 * This class is NOT a raw Kafka consumer.
 * It is a test abstraction.
 */
public final class KafkaTestConsumer {

    private static final Log log =
            Log.get(KafkaTestConsumer.class);

    private final KafkaConsumer<String, String> consumer;

    private KafkaTestConsumer(
            KafkaConsumer<String, String> consumer
    ) {
        this.consumer = consumer;
    }

    /* =========================================================
       FACTORY
       ========================================================= */

    public static KafkaTestConsumer create(String logicalName) {
        KafkaConsumer<String, String> consumer =
                KafkaClientFactory.createTestConsumer(logicalName);
        return new KafkaTestConsumer(consumer);
    }

    /* =========================================================
       SUBSCRIPTION
       ========================================================= */

    public KafkaTestConsumer subscribe(String topic) {
        Objects.requireNonNull(topic, "topic must not be null");

        log.info("📡 Subscribing to Kafka topic={}", topic);
        consumer.subscribe(Set.of(topic));
        return this;
    }

    /* =========================================================
       POLLING
       ========================================================= */

    /**
     * Poll until at least one record arrives or timeout occurs.
     */
    public List<ConsumerRecord<String, String>> pollUntilAny(
            Duration timeout
    ) {
        return pollUntil(timeout, r -> true, 1);
    }

    /**
     * Poll until a record matching predicate is found.
     */
    public List<ConsumerRecord<String, String>> pollUntil(
            Duration timeout,
            Predicate<ConsumerRecord<String, String>> filter
    ) {
        return pollUntil(timeout, filter, 1);
    }

    /**
     * Poll until N records matching predicate are found.
     */
    public List<ConsumerRecord<String, String>> pollUntil(
            Duration timeout,
            Predicate<ConsumerRecord<String, String>> filter,
            int expectedCount
    ) {
        Instant deadline = Instant.now().plus(timeout);
        List<ConsumerRecord<String, String>> matched =
                new ArrayList<>();

        log.info(
                "⏳ Polling Kafka (expected={}, timeout={}s)",
                expectedCount, timeout.toSeconds()
        );

        while (Instant.now().isBefore(deadline)) {

            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(500));

            for (ConsumerRecord<String, String> record : records) {
                if (filter.test(record)) {
                    matched.add(record);
                    log.debug(
                            "📨 Matched record topic={} key={}",
                            record.topic(), record.key()
                    );
                }
            }

            if (matched.size() >= expectedCount) {
                consumer.commitSync();
                log.info(
                        "✅ Received {} Kafka records",
                        matched.size()
                );
                return matched;
            }
        }

        fail("""
             ❌ Kafka records not received in time
             Expected: %d
             Received: %d
             """.formatted(expectedCount, matched.size()));

        return matched;
    }

    /* =========================================================
       COMMON FILTERS
       ========================================================= */

    public static Predicate<ConsumerRecord<String, String>>
    hasKey(String key) {
        return r -> key.equals(r.key());
    }

    public static Predicate<ConsumerRecord<String, String>>
    valueContains(String substring) {
        return r -> r.value() != null &&
                r.value().contains(substring);
    }

    public static Predicate<ConsumerRecord<String, String>>
    topicIs(String topic) {
        return r -> topic.equals(r.topic());
    }

    /* =========================================================
       CLEANUP
       ========================================================= */

    public void close() {
        try {
            consumer.close(Duration.ofSeconds(5));
            log.info("🧹 Kafka test consumer closed");
        } catch (Exception e) {
            log.warn("Failed to close Kafka consumer", e);
        }
    }
}
