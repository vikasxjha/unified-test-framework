package com.company.qa.unified.chaos;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.Log;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Central orchestrator for chaos experiments.
 *
 * Responsibilities:
 * - Start chaos experiments
 * - Ensure time-bound execution
 * - Automatically rollback on failure
 * - Provide safe primitives for tests
 *
 * Chaos is ALWAYS:
 * - scoped
 * - reversible
 * - logged
 */
public final class ChaosOrchestrator {

    private static final Log log = Log.get(ChaosOrchestrator.class);

    private ChaosOrchestrator() {
        // utility
    }

    /* =========================================================
       PUBLIC ENTRY POINTS
       ========================================================= */

    public static ChaosHandle injectLatency(
            String serviceName,
            Duration latency,
            Duration duration
    ) {
        return startChaos(
                ChaosScenario.latency(serviceName, latency, duration)
        );
    }

    public static ChaosHandle injectErrorRate(
            String serviceName,
            int httpStatus,
            int percentage,
            Duration duration
    ) {
        return startChaos(
                ChaosScenario.httpError(serviceName, httpStatus, percentage, duration)
        );
    }

    public static ChaosHandle killService(
            String serviceName,
            Duration duration
    ) {
        return startChaos(
                ChaosScenario.kill(serviceName, duration)
        );
    }

    public static ChaosHandle isolateNetwork(
            String serviceName,
            Duration duration
    ) {
        return startChaos(
                ChaosScenario.networkIsolation(serviceName, duration)
        );
    }

    /* =========================================================
       CORE ORCHESTRATION
       ========================================================= */

    public static ChaosHandle startChaos(ChaosScenario scenario) {
        validateEnvironment();

        String experimentId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        log.info("""
                🔥 Starting chaos experiment
                Experiment ID: {}
                Scenario: {}
                """, experimentId, scenario);

        try {
            ChaosClient client = ChaosClient.create(
                    EnvironmentConfig.get().getChaosEndpoint()
            );

            client.startExperiment(experimentId, scenario);

            return new ChaosHandle(
                    experimentId,
                    scenario,
                    startTime,
                    client
            );

        } catch (Exception e) {
            fail("Failed to start chaos experiment: " + e.getMessage());
            return null; // unreachable
        }
    }

    /* =========================================================
       SAFETY
       ========================================================= */

    private static void validateEnvironment() {
        String env = EnvironmentConfig.get().getEnvironmentName();

        if ("PROD".equalsIgnoreCase(env)) {
            fail("❌ Chaos experiments are NOT allowed in PROD");
        }
    }

    /* =========================================================
       CHAOS HANDLE
       ========================================================= */

    /**
     * Handle returned to tests.
     * Must be closed to rollback chaos.
     */
    public static final class ChaosHandle implements AutoCloseable {

        private final String experimentId;
        private final ChaosScenario scenario;
        private final Instant startTime;
        private final ChaosClient client;
        private boolean closed = false;

        private ChaosHandle(
                String experimentId,
                ChaosScenario scenario,
                Instant startTime,
                ChaosClient client
        ) {
            this.experimentId = experimentId;
            this.scenario = scenario;
            this.startTime = startTime;
            this.client = client;
        }

        public String getExperimentId() {
            return experimentId;
        }

        public ChaosScenario getScenario() {
            return scenario;
        }

        @Override
        public void close() {
            if (closed) return;

            try {
                client.stopExperiment(experimentId);
                log.info("""
                        🧯 Chaos experiment rolled back
                        Experiment ID: {}
                        Duration: {}s
                        """,
                        experimentId,
                        Duration.between(startTime, Instant.now()).toSeconds()
                );
            } catch (Exception e) {
                log.error("❌ Failed to rollback chaos experiment {}", experimentId, e);
            } finally {
                closed = true;
            }
        }
    }

    /* =========================================================
       CHAOS CLIENT (HTTP-BASED)
       ========================================================= */

    static final class ChaosClient {

        private final String endpoint;

        private ChaosClient(String endpoint) {
            this.endpoint = endpoint;
        }

        static ChaosClient create(String endpoint) {
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("Chaos endpoint not configured");
            }
            return new ChaosClient(endpoint);
        }

        void startExperiment(String id, ChaosScenario scenario) {
            HttpClient.post(
                    endpoint + "/experiments",
                    Map.of(
                            "id", id,
                            "scenario", scenario.toMap()
                    )
            );
        }

        void stopExperiment(String id) {
            HttpClient.delete(endpoint + "/experiments/" + id);
        }
    }

    /* =========================================================
       MINIMAL HTTP CLIENT (INTENTIONAL)
       ========================================================= */

    static final class HttpClient {

        static void post(String url, Map<String, Object> payload) {
            // Intentionally minimal – real impl can use:
            // - RestAssured
            // - OkHttp
            // - Apache HttpClient
            Log.get(HttpClient.class).info("POST {} payload={}", url, payload);
        }

        static void delete(String url) {
            Log.get(HttpClient.class).info("DELETE {}", url);
        }
    }
}
