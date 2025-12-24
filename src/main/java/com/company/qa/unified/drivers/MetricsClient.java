package com.company.qa.unified.drivers;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * MetricsClient is used for validating system metrics
 * exposed via Prometheus / observability endpoints.
 *
 * Supported use cases:
 * - API latency validation
 * - Error rate checks
 * - Throughput verification
 * - SLO / SLA assertions
 * - Canary & production smoke monitoring
 *
 * This client is READ-ONLY.
 */
public final class MetricsClient {

    private static final Log log =
            Log.get(MetricsClient.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private static final Duration DEFAULT_TIMEOUT =
            Duration.ofSeconds(10);

    private MetricsClient() {
        // utility
    }

    /* =========================================================
       PUBLIC QUERY API
       ========================================================= */

    /**
     * Execute a raw PromQL query.
     */
    public static MetricQueryResult query(String promQl) {

        try {
            String encoded =
                    URLEncoder.encode(promQl, StandardCharsets.UTF_8);

            String url =
                    ENV.getMetricsBaseUrl()
                            + "/api/v1/query?query=" + encoded;

            log.debug("Querying metrics: {}", promQl);

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(url).openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout((int) DEFAULT_TIMEOUT.toMillis());
            conn.setReadTimeout((int) DEFAULT_TIMEOUT.toMillis());
            conn.setRequestProperty(
                    "Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IllegalStateException(
                        "Metrics query failed: HTTP " + status);
            }

            Map<String, Object> response =
                    JsonUtils.fromJson(
                            conn.getInputStream(), Map.class);

            return MetricQueryResult.from(response);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to query metrics", e);
        }
    }

    /* =========================================================
       HIGH-LEVEL ASSERTIONS
       ========================================================= */

    /**
     * Assert API p95 latency is below threshold.
     */
    public static void assertLatencyBelow(
            String service,
            String endpoint,
            Duration maxLatency
    ) {
        String promQl = """
                histogram_quantile(
                  0.95,
                  sum(rate(http_server_requests_seconds_bucket{
                    service="%s",
                    uri="%s"
                  }[5m])) by (le)
                )
                """.formatted(service, endpoint);

        double latency = query(promQl).singleValue();

        log.info("p95 latency for {} {} = {}s",
                service, endpoint, latency);

        if (latency > maxLatency.toSeconds()) {
            fail("Latency SLA breached: " + latency + "s");
        }
    }

    /**
     * Assert error rate is below threshold.
     */
    public static void assertErrorRateBelow(
            String service,
            double maxPercent
    ) {
        String promQl = """
                sum(rate(http_server_requests_total{
                  service="%s",
                  status=~"5.."
                }[5m]))
                /
                sum(rate(http_server_requests_total{
                  service="%s"
                }[5m])) * 100
                """.formatted(service, service);

        double errorRate = query(promQl).singleValue();

        log.info("Error rate for {} = {}%",
                service, errorRate);

        if (errorRate > maxPercent) {
            fail("Error rate SLA breached: " + errorRate + "%");
        }
    }

    /**
     * Assert throughput above minimum QPS.
     */
    public static void assertThroughputAbove(
            String service,
            double minQps
    ) {
        String promQl = """
                sum(rate(http_server_requests_total{
                  service="%s"
                }[1m]))
                """.formatted(service);

        double qps = query(promQl).singleValue();

        log.info("Throughput for {} = {} QPS",
                service, qps);

        if (qps < minQps) {
            fail("Throughput too low: " + qps + " QPS");
        }
    }

    /* =========================================================
       RESULT MODEL
       ========================================================= */

    /**
     * Minimal abstraction over Prometheus query result.
     */
    public static final class MetricQueryResult {

        private final List<Result> results;

        private MetricQueryResult(List<Result> results) {
            this.results = results;
        }

        public static MetricQueryResult from(
                Map<String, Object> raw
        ) {
            Map<String, Object> data =
                    (Map<String, Object>) raw.get("data");

            List<Map<String, Object>> result =
                    (List<Map<String, Object>>) data.get("result");

            List<Result> parsed =
                    result.stream()
                            .map(Result::from)
                            .toList();

            return new MetricQueryResult(parsed);
        }

        /**
         * Expect exactly one value.
         */
        public double singleValue() {
            if (results.isEmpty()) {
                fail("No metric data returned");
            }
            return results.get(0).value();
        }
    }

    private record Result(double value) {

        static Result from(Map<String, Object> raw) {
            List<Object> value =
                    (List<Object>) raw.get("value");

            return new Result(
                    Double.parseDouble(value.get(1).toString())
            );
        }
    }
}
