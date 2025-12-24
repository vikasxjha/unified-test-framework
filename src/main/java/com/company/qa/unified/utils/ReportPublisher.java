package com.company.qa.unified.utils;

import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * ReportPublisher
 *
 * Centralized reporting utility for:
 * - Test steps
 * - Attachments (screenshots, logs, JSON, metrics)
 * - Errors & diagnostics
 *
 * Designed to work even if Allure is NOT present.
 */
public final class ReportPublisher {

    private static final Log log =
            Log.get(ReportPublisher.class);

    private static final boolean ALLURE_AVAILABLE =
            isAllureAvailable();

    private ReportPublisher() {
        // utility class
    }

    /* =========================================================
       STEP REPORTING
       ========================================================= */

    public static void step(String message) {
        log.info("🧪 STEP: {}", message);

        if (ALLURE_AVAILABLE) {
            Allure.step(message);
        }
    }

    public static void step(String message, Runnable action) {
        log.info("🧪 STEP: {}", message);

        if (ALLURE_AVAILABLE) {
            Allure.step(message, action);
        } else {
            action.run();
        }
    }

    /* =========================================================
       ATTACHMENTS
       ========================================================= */

    public static void attachText(String name, String content) {
        log.debug("📎 Attaching text: {}", name);

        if (ALLURE_AVAILABLE) {
            Allure.addAttachment(
                    name,
                    "text/plain",
                    new ByteArrayInputStream(
                            content.getBytes(StandardCharsets.UTF_8)
                    ),
                    ".txt"
            );
        }
    }

    public static void attachJson(String name, String json) {
        log.debug("📎 Attaching JSON: {}", name);

        if (ALLURE_AVAILABLE) {
            Allure.addAttachment(
                    name,
                    "application/json",
                    new ByteArrayInputStream(
                            json.getBytes(StandardCharsets.UTF_8)
                    ),
                    ".json"
            );
        }
    }

    public static void attachScreenshot(String name, byte[] imageBytes) {
        log.debug("📎 Attaching screenshot: {}", name);

        if (ALLURE_AVAILABLE && imageBytes != null) {
            Allure.addAttachment(
                    name,
                    "image/png",
                    new ByteArrayInputStream(imageBytes),
                    ".png"
            );
        }
    }

    public static void attachBinary(
            String name,
            byte[] data,
            String mimeType,
            String extension
    ) {
        log.debug("📎 Attaching binary: {}", name);

        if (ALLURE_AVAILABLE && data != null) {
            Allure.addAttachment(
                    name,
                    mimeType,
                    new ByteArrayInputStream(data),
                    extension
            );
        }
    }

    /* =========================================================
       API / EVENT / METRICS HELPERS
       ========================================================= */

    public static void attachApiRequest(
            String endpoint,
            String method,
            String payload
    ) {
        attachText(
                "API Request",
                String.format(
                        "Time: %s\nMethod: %s\nEndpoint: %s\nPayload:\n%s",
                        Instant.now(),
                        method,
                        endpoint,
                        payload
                )
        );
    }

    public static void attachApiResponse(
            int status,
            String body
    ) {
        attachText(
                "API Response",
                String.format(
                        "Status: %d\nBody:\n%s",
                        status,
                        body
                )
        );
    }

    public static void attachEventPayload(
            String topic,
            String payload
    ) {
        attachJson(
                "Kafka Event: " + topic,
                payload
        );
    }

    public static void attachMetrics(
            String metricName,
            Map<String, Object> values
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Metric: ").append(metricName).append("\n");

        values.forEach((k, v) ->
                sb.append(k).append(" = ").append(v).append("\n")
        );

        attachText("Metrics", sb.toString());
    }

    /* =========================================================
       FAILURE HANDLING
       ========================================================= */

    public static void publishFailure(
            String reason,
            Throwable error
    ) {
        log.error("❌ FAILURE: {}", reason, error);

        attachText(
                "Failure Reason",
                reason + "\n\n" + error
        );
    }

    /* =========================================================
       INTERNAL
       ========================================================= */

    private static boolean isAllureAvailable() {
        try {
            Class.forName("io.qameta.allure.Allure");
            return true;
        } catch (ClassNotFoundException e) {
            log.warn("Allure not found on classpath. Reporting degraded.");
            return false;
        }
    }
}
