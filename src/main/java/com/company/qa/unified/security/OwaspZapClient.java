package com.company.qa.unified.security;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.company.qa.unified.utils.JsonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * OwaspZapClient
 *
 * Client for controlling OWASP ZAP via REST API.
 *
 * Supports:
 * - Baseline scan
 * - Active scan
 * - Context include/exclude
 * - Auth headers
 * - Alert retrieval & severity gating
 *
 * Assumptions:
 * - ZAP is running (Docker or local)
 * - ZAP API key is optional (recommended in CI)
 */
public class OwaspZapClient {

    private static final Log log = Log.get(OwaspZapClient.class);

    private final HttpClient http;
    private final String zapBaseUrl;
    private final String apiKey;

    public enum Risk {
        LOW, MEDIUM, HIGH
    }

    public OwaspZapClient(String zapBaseUrl, String apiKey) {
        this.zapBaseUrl = zapBaseUrl.endsWith("/")
                ? zapBaseUrl.substring(0, zapBaseUrl.length() - 1)
                : zapBaseUrl;
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /* =========================================================
       BASELINE SCAN
       ========================================================= */

    public void runBaselineScan(String targetUrl) {
        log.info("🛡 Running ZAP baseline scan on {}", targetUrl);

        String url = zap("/JSON/ascan/action/scan/",
                Map.of("url", targetUrl));

        ReportPublisher.step("ZAP baseline scan started");
        waitForScanCompletion(url);
    }

    /* =========================================================
       ACTIVE SCAN
       ========================================================= */

    public void runActiveScan(String targetUrl) {
        log.info("🔥 Running ZAP active scan on {}", targetUrl);

        String url = zap("/JSON/ascan/action/scan/",
                Map.of(
                        "url", targetUrl,
                        "recurse", "true",
                        "inScopeOnly", "true"
                ));

        ReportPublisher.step("ZAP active scan started");
        waitForScanCompletion(url);
    }

    /* =========================================================
       CONTEXT CONFIGURATION
       ========================================================= */

    public void includeInContext(String context, String regex) {
        zap("/JSON/context/action/includeInContext/",
                Map.of(
                        "contextName", context,
                        "regex", regex
                ));
        log.info("Added include regex [{}] to context {}", regex, context);
    }

    public void excludeFromContext(String context, String regex) {
        zap("/JSON/context/action/excludeFromContext/",
                Map.of(
                        "contextName", context,
                        "regex", regex
                ));
        log.info("Added exclude regex [{}] to context {}", regex, context);
    }

    /* =========================================================
       AUTH HEADERS
       ========================================================= */

    public void setAuthHeader(String header, String value) {
        zap("/JSON/replacer/action/addRule/",
                Map.of(
                        "description", "AuthHeader",
                        "enabled", "true",
                        "matchType", "REQ_HEADER",
                        "matchRegex", "false",
                        "matchString", header,
                        "replacement", value
                ));

        log.info("Configured auth header for ZAP scans");
    }

    /* =========================================================
       ALERTS & GATING
       ========================================================= */

    public void assertNoAlertsAbove(Risk riskThreshold) {
        String response = zap("/JSON/core/view/alerts/",
                Map.of("baseurl", ""));

        ReportPublisher.attachJson(
                "ZAP Alerts",
                JsonUtils.pretty(response)
        );

        ZapAlertSummary summary =
                ZapAlertSummary.fromJson(response);

        if (summary.hasRiskAbove(riskThreshold)) {
            throw new AssertionError(
                    "Security alerts found above threshold: "
                            + riskThreshold);
        }

        log.info("✅ ZAP security gate passed");
    }

    /* =========================================================
       SCAN STATUS
       ========================================================= */

    private void waitForScanCompletion(String scanUrl) {

        try {
            while (true) {
                String statusJson =
                        zap("/JSON/ascan/view/status/", Map.of());

                int progress =
                        Integer.parseInt(
                                JsonUtils.read(statusJson, "status"));

                log.info("ZAP scan progress: {}%", progress);

                if (progress >= 100) {
                    break;
                }
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            throw new IllegalStateException("ZAP scan monitoring failed", e);
        }
    }

    /* =========================================================
       CORE HTTP
       ========================================================= */

    private String zap(String path, Map<String, String> params) {

        try {
            StringBuilder url = new StringBuilder(zapBaseUrl)
                    .append(path)
                    .append("?");

            if (apiKey != null && !apiKey.isBlank()) {
                url.append("apikey=")
                        .append(encode(apiKey))
                        .append("&");
            }

            for (Map.Entry<String, String> e : params.entrySet()) {
                url.append(e.getKey())
                        .append("=")
                        .append(encode(e.getValue()))
                        .append("&");
            }

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url.toString()))
                            .GET()
                            .timeout(Duration.ofSeconds(30))
                            .build();

            HttpResponse<String> response =
                    http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "ZAP API call failed: " + response.body());
            }

            return response.body();

        } catch (Exception e) {
            throw new IllegalStateException("ZAP API call error", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /* =========================================================
       ALERT SUMMARY MODEL
       ========================================================= */

    private static class ZapAlertSummary {

        private final int high;
        private final int medium;
        private final int low;

        ZapAlertSummary(int high, int medium, int low) {
            this.high = high;
            this.medium = medium;
            this.low = low;
        }

        static ZapAlertSummary fromJson(String json) {
            int high = JsonUtils.count(json, "\"risk\":\"High\"");
            int medium = JsonUtils.count(json, "\"risk\":\"Medium\"");
            int low = JsonUtils.count(json, "\"risk\":\"Low\"");
            return new ZapAlertSummary(high, medium, low);
        }

        boolean hasRiskAbove(Risk threshold) {
            return switch (threshold) {
                case LOW -> high > 0 || medium > 0 || low > 0;
                case MEDIUM -> high > 0 || medium > 0;
                case HIGH -> high > 0;
            };
        }
    }
}
