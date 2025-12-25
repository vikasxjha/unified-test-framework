package com.company.qa.unified.performance;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * PerfTimer
 *
 * High-precision timing utility for:
 * - API calls
 * - Web transactions
 * - Mobile actions
 * - Background jobs
 *
 * Features:
 * - Nanosecond precision
 * - Thread-safe aggregation
 * - Works with lambdas
 * - CI-safe reporting
 */
public class PerfTimer {

    private static final Log log =
            Log.get(PerfTimer.class);

    private final String metricName;
    private final List<Long> measurementsMillis =
            Collections.synchronizedList(new ArrayList<>());

    private Instant suiteStart;
    private Instant suiteEnd;

    public PerfTimer(String metricName) {
        this.metricName = metricName;
    }

    /* =========================================================
       SUITE / WINDOW CONTROL
       ========================================================= */

    public void startSuite() {
        suiteStart = Instant.now();
        log.info("⏱ Started performance suite: {}", metricName);
    }

    public void endSuite() {
        suiteEnd = Instant.now();
        log.info("⏱ Finished performance suite: {}", metricName);

        ReportPublisher.attachText(
                "Performance Summary - " + metricName,
                summary()
        );
    }

    public Duration getTotalDuration() {
        if (suiteStart == null || suiteEnd == null) {
            return Duration.ZERO;
        }
        return Duration.between(suiteStart, suiteEnd);
    }

    /* =========================================================
       MEASUREMENT
       ========================================================= */

    /**
     * Measure execution time of a Runnable.
     */
    public void measure(Runnable action) {
        long start = System.nanoTime();
        try {
            action.run();
        } finally {
            record(start);
        }
    }

    /**
     * Measure execution time of a Callable and return result.
     */
    public <T> T measure(Callable<T> action) {
        long start = System.nanoTime();
        try {
            return action.call();
        } catch (Exception e) {
            throw new RuntimeException("PerfTimer execution failed", e);
        } finally {
            record(start);
        }
    }

    /**
     * Manually record elapsed time.
     */
    public void recordMillis(long elapsedMillis) {
        measurementsMillis.add(elapsedMillis);
    }

    private void record(long startNano) {
        long elapsedMillis =
                (System.nanoTime() - startNano) / 1_000_000;
        measurementsMillis.add(elapsedMillis);
        log.debug("⏱ {} recorded {} ms", metricName, elapsedMillis);
    }

    /* =========================================================
       ACCESSORS
       ========================================================= */

    public List<Long> getMeasurements() {
        return new ArrayList<>(measurementsMillis);
    }

    public int getCount() {
        return measurementsMillis.size();
    }

    /* =========================================================
       STATISTICS
       ========================================================= */

    public long avg() {
        if (measurementsMillis.isEmpty()) {
            return 0;
        }
        return (long) measurementsMillis.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    public long min() {
        return measurementsMillis.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
    }

    public long max() {
        return measurementsMillis.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
    }

    public long percentile(int percentile) {
        if (measurementsMillis.isEmpty()) {
            return 0;
        }

        List<Long> sorted =
                new ArrayList<>(measurementsMillis);
        Collections.sort(sorted);

        int index =
                (int) Math.ceil(
                        percentile / 100.0 * sorted.size()
                ) - 1;

        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    /* =========================================================
       SUMMARY
       ========================================================= */

    public String summary() {
        return String.format(
                "Metric: %s\n" +
                        "Samples: %d\n" +
                        "Avg: %d ms\n" +
                        "Min: %d ms\n" +
                        "P95: %d ms\n" +
                        "P99: %d ms\n" +
                        "Max: %d ms\n" +
                        "Total Duration: %ds",
                metricName,
                getCount(),
                avg(),
                min(),
                percentile(95),
                percentile(99),
                max(),
                getTotalDuration().getSeconds()
        );
    }

    /* =========================================================
       ASSERTION BRIDGE
       ========================================================= */

    public void assertP95Under(long thresholdMillis) {
        PerfAssertionUtils.assertP95LatencyUnder(
                getMeasurements(),
                thresholdMillis,
                metricName
        );
    }

    public void assertAvgUnder(long thresholdMillis) {
        PerfAssertionUtils.assertAvgLatencyUnder(
                getMeasurements(),
                thresholdMillis,
                metricName
        );
    }
}
