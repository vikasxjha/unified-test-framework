package com.company.qa.unified.chaos;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.JsonUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Low-level HTTP client for communicating with Chaos infrastructure.
 *
 * Responsibilities:
 * - Send chaos experiment start/stop requests
 * - Serialize ChaosScenario payloads
 * - Perform minimal validation & logging
 *
 * This class:
 * - DOES NOT contain environment checks
 * - DOES NOT manage lifecycle
 * - DOES NOT contain test logic
 *
 * Those are handled by ChaosOrchestrator.
 */
public final class ChaosClient {

    private static final Log log =
            Log.get(ChaosClient.class);

    private final String chaosEndpoint;

    ChaosClient(String chaosEndpoint) {
        if (chaosEndpoint == null || chaosEndpoint.isBlank()) {
            throw new IllegalArgumentException(
                    "Chaos endpoint must not be null or empty");
        }
        this.chaosEndpoint = chaosEndpoint;
    }

    /* =========================================================
       FACTORY
       ========================================================= */

    public static ChaosClient create(String chaosEndpoint) {
        return new ChaosClient(chaosEndpoint);
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Start a chaos experiment.
     */
    public void startExperiment(
            String experimentId,
            ChaosScenario scenario
    ) {
        log.info("🔥 Starting chaos experiment id={}", experimentId);

        Map<String, Object> payload = Map.of(
                "experimentId", experimentId,
                "scenario", scenario.toMap()
        );

        post("/experiments/start", payload);
    }

    /**
     * Stop a chaos experiment.
     */
    public void stopExperiment(String experimentId) {
        log.info("🧯 Stopping chaos experiment id={}", experimentId);
        post("/experiments/stop", Map.of("experimentId", experimentId));
    }

    /* =========================================================
       HTTP TRANSPORT
       ========================================================= */

    private void post(
            String path,
            Map<String, Object> payload
    ) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(chaosEndpoint + path);
            String body = JsonUtils.toJson(payload);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(10_000);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Content-Type", "application/json");
            connection.setRequestProperty(
                    "User-Agent", "unified-test-framework/chaos");

            connection.getOutputStream()
                    .write(body.getBytes(StandardCharsets.UTF_8));

            int status = connection.getResponseCode();

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Chaos API call failed: HTTP " + status);
            }

            log.info("Chaos API call succeeded: {}", path);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Chaos API request failed: " + path, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

