package com.company.qa.unified.contracts;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.EnvironmentType;
import com.company.qa.unified.utils.Log;

/**
 * Central Pact configuration for contract tests.
 *
 * Responsibilities:
 * - Resolve Pact Broker configuration
 * - Enforce environment safety
 * - Provide common settings for consumer/provider tests
 *
 * This class contains NO test logic.
 */
public final class PactConfig {

    private static final Log log = Log.get(PactConfig.class);

    private static final EnvironmentConfig ENV = EnvironmentConfig.get();

    /* =========================================================
       JVM PROPERTY KEYS
       ========================================================= */

    private static final String PACT_BROKER_URL = "pact.broker.url";
    private static final String PACT_BROKER_TOKEN = "pact.broker.token";
    private static final String PACT_PUBLISH = "pact.publish";
    private static final String PACT_CONSUMER_VERSION = "pact.consumer.version";

    private static final String DEFAULT_CONSUMER_VERSION = "local";

    private PactConfig() {
        // utility
    }

    /* =========================================================
       BROKER CONFIG
       ========================================================= */

    public static String brokerUrl() {
        return System.getProperty(PACT_BROKER_URL);
    }

    public static String brokerToken() {
        return System.getProperty(PACT_BROKER_TOKEN);
    }

    public static boolean isBrokerConfigured() {
        return brokerUrl() != null && brokerToken() != null;
    }

    /* =========================================================
       PUBLISHING RULES
       ========================================================= */

    /**
     * Whether pacts should be published to the broker.
     *
     * Allowed:
     * - QA
     * - STAGE
     *
     * NEVER allowed in PROD.
     */
    public static boolean shouldPublishPacts() {
        boolean requested =
                Boolean.parseBoolean(
                        System.getProperty(PACT_PUBLISH, "false"));

        EnvironmentType env = EnvironmentType.current();

        if (requested && env.isProd()) {
            throw new IllegalStateException(
                    "❌ Pact publishing is NOT allowed in PROD");
        }

        return requested;
    }

    /* =========================================================
       CONSUMER VERSIONING
       ========================================================= */

    public static String consumerVersion() {
        return System.getProperty(
                PACT_CONSUMER_VERSION,
                DEFAULT_CONSUMER_VERSION
        );
    }

    /* =========================================================
       LOGGING / DEBUG
       ========================================================= */

    public static void logSummary() {
        log.info("""
                📜 Pact Configuration
                ---------------------
                Environment        : {}
                Broker Configured  : {}
                Publish Enabled    : {}
                Consumer Version   : {}
                """,
                EnvironmentType.current(),
                isBrokerConfigured(),
                shouldPublishPacts(),
                consumerVersion()
        );
    }
}
