package com.company.qa.unified.performance;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * PerfAssertionUtils
 *
 * Central performance assertion helper used by:
 * - API perf tests
 * - Web transaction timing
 * - Mobile flows
 * - Background jobs
 *
 * RULES:
 * ✅ Deterministic
 * ✅ CI-safe
 * ❌ No sleeps
 * ❌ No environment coupling
 */
public final class PerfAssertionUtils {

    private static final Log log =
            Log.get(PerfAssertionUtils.class);

    private PerfAssertionUtils() {
        // utility class
    }

    /* =========================================================
       LATENCY ASSERTIONS
       ========================================================= */

    public static void assertAvgLatencyUnder(
            List<Long> latenciesMillis,
            long maxAllowedMillis,
            String metricName
    ) {

        long avg = average(latenciesMillis);

        publish(metricName, "avgLatencyMs", avg, maxAllowedMillis);

        if (avg > maxAllowedMillis) {
            throw new AssertionError(
                    metricName + " average latency exceeded: "
                            + avg + "ms > " + maxAllowedMillis + "ms"
            );
        }
    }

    public static void assertP95LatencyUnder(
            List<Long> latenciesMillis,
            long maxAllowedMillis,
            String metricName
    ) {

        long p95 = percentile(latenciesMillis, 95);

        publish(metricName, "p95LatencyMs", p95, maxAllowedMillis);

        if (p95 > maxAllowedMillis) {
            throw new AssertionError(
                    metricName + " p95 latency exceeded: "
                            + p95 + "ms > " + maxAllowedMillis + "ms"
            );
        }
    }

    public static void assertP99LatencyUnder(
            List<Long> latenciesMillis,
            long maxAllowedMillis,
            String metricName
    ) {

        long p99 = percentile(latenciesMillis, 99);

        publish(metricName, "p99LatencyMs", p99, maxAllowedMillis);

        if (p99 > maxAllowedMillis) {
            throw new AssertionError(
                    metricName + " p99 latency exceeded: "
                            + p99 + "ms > " + maxAllowedMillis + "ms"
            );
        }
    }

    /* =========================================================
       ERROR RATE ASSERTIONS
       ========================================================= */

    public static void assertErrorRateUnder(
            int totalRequests,
            int errorCount,
            double maxErrorRatePercent,
            String metricName
    ) {

        double errorRate =
                totalRequests == 0
                        ? 0
                        : (errorCount * 100.0) / totalRequests;

        ReportPublisher.attachText(
                "Performance Error Rate",
                String.format(
                        "%s\nTotal=%d Errors=%d ErrorRate=%.2f%% Threshold=%.2f%%",
                        metricName,
                        totalRequests,
                        errorCount,
                        errorRate,
                        maxErrorRatePercent
                )
        );

        if (errorRate > maxErrorRatePercent) {
            throw new AssertionError(
                    metricName + " error rate exceeded: "
                            + errorRate + "% > " + maxErrorRatePercent + "%"
            );
        }
    }

    /* =========================================================
       THROUGHPUT ASSERTIONS
       ========================================================= */

    public static void assertThroughputAtLeast(
            int totalRequests,
            Duration duration,
            double minRequestsPerSecond,
            String metricName
    ) {

        double rps =
                duration.isZero()
                        ? 0
                        : totalRequests / (double) duration.getSeconds();

        ReportPublisher.attachText(
                "Performance Throughput",
                String.format(
                        "%s\nRequests=%d Duration=%ds RPS=%.2f Threshold=%.2f",
                        metricName,
                        totalRequests,
                        duration.getSeconds(),
                        rps,
                        minRequestsPerSecond
                )
        );

        if (rps < minRequestsPerSecond) {
            throw new AssertionError(
                    metricName + " throughput below threshold: "
                            + rps + " < " + minRequestsPerSecond
            );
        }
    }

    /* =========================================================
       INTERNAL CALCULATIONS
       ========================================================= */

    private static long average(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        return (long) values.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    private static long percentile(
            List<Long> values,
            int percentile
    ) {

        if (values == null || values.isEmpty()) {
            return 0;
        }

        Collections.sort(values);

        int index =
                (int) Math.ceil(
                        percentile / 100.0 * values.size()
                ) - 1;

        index = Math.max(0, Math.min(index, values.size() - 1));

        return values.get(index);
    }

    private static void publish(
            String metricName,
            String metricType,
            long actual,
            long threshold
    ) {

        log.info(
                "📊 {} {} = {} (threshold={})",
                metricName,
                metricType,
                actual,
                threshold
        );

        ReportPublisher.attachText(
                "Performance Metric",
                String.format(
                        "%s\nMetric=%s\nActual=%d\nThreshold=%d",
                        metricName,
                        metricType,
                        actual,
                        threshold
                )
        );
    }
}
