package com.company.qa.unified.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

/**
 * NotificationUtils
 *
 * Central notification utility for:
 * - Test failures
 * - CI status updates
 * - Slack / Webhook alerts
 * - In-app / push notification validation hooks
 *
 * This class MUST NOT fail tests.
 */
public final class NotificationUtils {

    private static final Log log =
            Log.get(NotificationUtils.class);

    private static final HttpClient httpClient =
            HttpClient.newHttpClient();

    private NotificationUtils() {
        // Utility class
    }

    /* =========================================================
       CI / TEST STATUS NOTIFICATIONS
       ========================================================= */

    public static void notifyTestFailure(
            String testName,
            String environment,
            Throwable error
    ) {

        String message =
                String.format(
                        "❌ Test Failed\n" +
                                "Test: %s\n" +
                                "Env: %s\n" +
                                "Time: %s\n" +
                                "Error: %s",
                        testName,
                        environment,
                        Instant.now(),
                        error.getMessage()
                );

        log.error(message, error);

        ReportPublisher.attachText("Failure Notification", message);

        sendSlackNotification(message);
    }

    public static void notifyTestSuccess(
            String testName,
            String environment
    ) {

        String message =
                String.format(
                        "✅ Test Passed\n" +
                                "Test: %s\n" +
                                "Env: %s\n" +
                                "Time: %s",
                        testName,
                        environment,
                        Instant.now()
                );

        log.info(message);
        sendSlackNotification(message);
    }

    /* =========================================================
       SLACK / WEBHOOK
       ========================================================= */

    public static void sendSlackNotification(String message) {

        String webhookUrl =
                System.getProperty("slack.webhook");

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Slack webhook not configured. Skipping notification.");
            return;
        }

        try {
            String payload =
                    "{ \"text\": \"" +
                            escape(message) +
                            "\" }";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(webhookUrl))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build();

            httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            log.info("📣 Slack notification sent");

        } catch (Exception e) {
            log.warn("Failed to send Slack notification", e);
        }
    }

    /* =========================================================
       GENERIC WEBHOOK (TEAMS / CUSTOM)
       ========================================================= */

    public static void sendWebhook(
            String url,
            Map<String, Object> payload
    ) {

        try {
            String json =
                    JsonUtils.toJson(payload);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            log.info("🔔 Webhook sent to {}", url);

        } catch (Exception e) {
            log.warn("Failed to send webhook", e);
        }
    }

    /* =========================================================
       IN-APP / PUSH VALIDATION HOOKS
       ========================================================= */

    /**
     * Used by tests to log expected notifications.
     */
    public static void expectNotification(
            String channel,
            String title,
            String body
    ) {

        String message =
                String.format(
                        "📨 Expected Notification\n" +
                                "Channel: %s\n" +
                                "Title: %s\n" +
                                "Body: %s",
                        channel,
                        title,
                        body
                );

        log.info(message);
        ReportPublisher.attachText("Expected Notification", message);
    }

    /**
     * Used by Kafka / Event tests.
     */
    public static void receivedNotificationEvent(
            String topic,
            String payload
    ) {

        log.info("📬 Notification event received from {}", topic);

        ReportPublisher.attachEventPayload(topic, payload);
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
