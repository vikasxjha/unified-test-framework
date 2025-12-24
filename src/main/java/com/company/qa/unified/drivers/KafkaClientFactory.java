package com.company.qa.unified.drivers;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.Log;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Central factory for Kafka producers and consumers used in tests.
 *
 * Supports:
 * - Test consumers with isolated consumer groups
 * - Optional producers for event injection
 * - Thread-safe caching
 *
 * RULE:
 * ❌ Tests must NOT configure Kafka clients directly
 * ✅ Tests must ALWAYS use KafkaClientFactory
 */
public final class KafkaClientFactory {

    private static final Log log =
            Log.get(KafkaClientFactory.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private static final ConcurrentMap<String, KafkaConsumer<String, String>>
            CONSUMERS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, KafkaProducer<String, String>>
            PRODUCERS = new ConcurrentHashMap<>();

    private KafkaClientFactory() {
        // utility
    }

    /* =========================================================
       CONSUMERS
       ========================================================= */

    /**
     * Create or reuse a Kafka consumer with a unique group ID.
     *
     * Each test SHOULD use a unique consumer group
     * to avoid flakiness.
     */
    public static KafkaConsumer<String, String>
    createTestConsumer(String logicalName) {

        return CONSUMERS.computeIfAbsent(logicalName, name -> {
            log.info("Creating Kafka test consumer: {}", name);

            Properties props = baseConsumerProps();
            props.put(
                    ConsumerConfig.GROUP_ID_CONFIG,
                    "test-" + name + "-" + UUID.randomUUID()
            );

            KafkaConsumer<String, String> consumer =
                    new KafkaConsumer<>(props);

            return consumer;
        });
    }

    /* =========================================================
       PRODUCERS
       ========================================================= */

    /**
     * Kafka producer for test event publishing.
     * (Used rarely; mostly consumers are used)
     */
    public static KafkaProducer<String, String>
    createTestProducer(String logicalName) {

        return PRODUCERS.computeIfAbsent(logicalName, name -> {
            log.info("Creating Kafka test producer: {}", name);

            Properties props = baseProducerProps();
            return new KafkaProducer<>(props);
        });
    }

    /* =========================================================
       BASE CONFIGURATION
       ========================================================= */

    private static Properties baseConsumerProps() {

        Properties props = new Properties();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ENV.getKafkaBootstrapServers()
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName()
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName()
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                "false"
        );

        props.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                50
        );

        props.put(
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                15000
        );

        props.put(
                ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                30000
        );

        return props;
    }

    private static Properties baseProducerProps() {

        Properties props = new Properties();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ENV.getKafkaBootstrapServers()
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        props.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );

        props.put(
                ProducerConfig.RETRIES_CONFIG,
                3
        );

        props.put(
                ProducerConfig.LINGER_MS_CONFIG,
                5
        );

        return props;
    }

    /* =========================================================
       CLEANUP
       ========================================================= */

    /**
     * Close all Kafka clients.
     * Call once after test suite execution.
     */
    public static void shutdownAll() {

        log.info("Shutting down Kafka test clients");

        CONSUMERS.values().forEach(consumer -> {
            try {
                consumer.close(Duration.ofSeconds(5));
            } catch (Exception ignored) {}
        });

        PRODUCERS.values().forEach(producer -> {
            try {
                producer.close(Duration.ofSeconds(5));
            } catch (Exception ignored) {}
        });

        CONSUMERS.clear();
        PRODUCERS.clear();
    }
}
