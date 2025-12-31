package com.company.qa.unified.utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;

import java.nio.file.Path;

/**
 * Utility class for Playwright Trace Viewer.
 *
 * Responsibilities:
 * - Start tracing with standard options
 * - Stop tracing and export trace.zip
 *
 * Traces help debug:
 * - Flaky tests
 * - CI failures
 * - Timing & race conditions
 */
public final class TraceUtil {

    private TraceUtil() {
        // Utility class
    }

    /**
     * Starts Playwright tracing.
     */
    public static void startTracing(BrowserContext context) {
        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(false)
        );
    }

    /**
     * Stops tracing and saves trace file.
     */
    public static void stopTracing(BrowserContext context, Path tracePath) {
        context.tracing().stop(
                new Tracing.StopOptions()
                        .setPath(tracePath)
        );
    }
}
