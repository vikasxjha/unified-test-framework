package com.company.qa.unified.metrics;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * PrometheusMetricsClient
 *
 * Responsibilities:
 * - Execute PromQL instant & range queries
 * - Provide metric-level assertions
 * - Support SLO / SLA validations
 *
 * READ-ONLY client.
 */
public final class PrometheusMetricsClient {

    private static final Log log =
            Log.get(PrometheusMetricsClient.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private static final Duration DEFAULT_TIMEOUT =
            Duration.ofSeconds(10);

    private PrometheusMetricsClient() {
        // utility
    }

    /* =========================================================
       INSTANT QUERY
       ========================================================= */

    public static PromQueryResult query(String promQl) {

        try {
            String encoded =
                    URLEncoder.encode(promQl, StandardCharsets.UTF_8);

            String url =
                    ENV.getMetricsBaseUrl()
                            + "/api/v1/query?query=" + encoded;

            log.debug("📊 Prometheus instant query: {}", promQl);

            return execute(url);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed Prometheus instant query", e);
        }
    }

    /* =========================================================
       RANGE QUERY
       ========================================================= */

    public static PromQueryResult rangeQuery(
            String promQl,
            Instant start,
            Instant end,
            Duration step
    ) {

        try {
            String encoded =
                    URLEncoder.encode(promQl, StandardCharsets.UTF_8);

            String url =
                    ENV.getMetricsBaseUrl()
                            + "/api/v1/query_range"
                            + "?query=" + encoded
                            + "&start=" + start.getEpochSecond()
                            + "&end=" + end.getEpochSecond()
                            + "&step=" + step.toSeconds();

            log.debug("📈 Prometheus range query: {}", promQl);

            return execute(url);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed Prometheus range query", e);
        }
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    public static void assertGaugeAbove(
            String promQl,
            double minValue
    ) {
        double value = query(promQl).singleValue();

        log.info("Gauge value={} expected>={}", value, minValue);

        if (value < minValue) {
            fail("Metric below expected value: " + value);
        }
    }

    public static void assertGaugeBelow(
            String promQl,
            double maxValue
    ) {
        double value = query(promQl).singleValue();

        log.info("Gauge value={} expected<={}", value, maxValue);

        if (value > maxValue) {
            fail("Metric above expected value: " + value);
        }
    }

    public static void assertIncrease(
            String promQl,
            Duration window,
            double minIncrease
    ) {
        String increaseQl =
                "increase(" + promQl + "[" + window.toMinutes() + "m])";

        double value = query(increaseQl).singleValue();

        log.info("Metric increase={} expected>={}", value, minIncrease);

        if (value < minIncrease) {
            fail("Metric did not increase as expected");
        }
    }

    /* =========================================================
       HTTP EXECUTION
       ========================================================= */

    private static PromQueryResult execute(String url) {

        try {
            HttpURLConnection conn =
                    (HttpURLConnection) new URL(url).openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout((int) DEFAULT_TIMEOUT.toMillis());
            conn.setReadTimeout((int) DEFAULT_TIMEOUT.toMillis());
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IllegalStateException(
                        "Prometheus query failed: HTTP " + status);
            }

            Map<String, Object> raw =
                    JsonUtils.fromJson(
                            conn.getInputStream(), Map.class);

            return PromQueryResult.from(raw);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Prometheus query execution failed", e);
        }
    }

    /* =========================================================
       RESULT MODEL
       ========================================================= */

    public static final class PromQueryResult {

        private final List<MetricResult> results;

        private PromQueryResult(List<MetricResult> results) {
            this.results = results;
        }

        public static PromQueryResult from(
                Map<String, Object> raw
        ) {
            Map<String, Object> data =
                    (Map<String, Object>) raw.get("data");

            List<Map<String, Object>> result =
                    (List<Map<String, Object>>) data.get("result");

            List<MetricResult> parsed =
                    result.stream()
                            .map(MetricResult::from)
                            .toList();

            return new PromQueryResult(parsed);
        }

        /**
         * Expect exactly one time-series value.
         */
        public double singleValue() {
            if (results.isEmpty()) {
                fail("No metric data returned from Prometheus");
            }
            return results.get(0).latestValue();
        }

        public List<MetricResult> all() {
            return results;
        }
    }

    public static final class MetricResult {

        private final Map<String, String> labels;
        private final List<List<Object>> values;

        private MetricResult(
                Map<String, String> labels,
                List<List<Object>> values
        ) {
            this.labels = labels;
            this.values = values;
        }

        static MetricResult from(Map<String, Object> raw) {
            return new MetricResult(
                    (Map<String, String>) raw.get("metric"),
                    (List<List<Object>>) raw.get("values")
            );
        }

        public double latestValue() {
            List<Object> last =
                    values.get(values.size() - 1);
            return Double.parseDouble(last.get(1).toString());
        }

        public Map<String, String> labels() {
            return labels;
        }
    }
}
