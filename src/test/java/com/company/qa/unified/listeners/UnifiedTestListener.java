package com.company.qa.unified.listeners;

import com.company.qa.unified.base.UsesPlaywrightPage;
import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.company.qa.unified.utils.NotificationUtils;
import com.company.qa.unified.utils.DateTimeUtils;
import com.company.qa.unified.performance.PerfTimer;

import com.microsoft.playwright.Page;
import org.testng.*;

import java.util.Optional;

/**
 * Unified TestNG Listener
 *
 * Responsibilities:
 * - Logging
 * - Screenshots
 * - Metrics
 * - Notifications
 * - Reporting
 * - Failure diagnostics
 */
public class UnifiedTestListener
        implements ITestListener, ISuiteListener, IRetryAnalyzer {

    private static final Log log = Log.get(UnifiedTestListener.class);

    /* =========================================================
       SUITE LEVEL
       ========================================================= */

    @Override
    public void onStart(ISuite suite) {
        log.info("🚀 Test Suite Started: {}", suite.getName());
        ReportPublisher.startSuite(suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("✅ Test Suite Finished: {}", suite.getName());
        ReportPublisher.endSuite();

        if (RuntimeConfig.NOTIFICATIONS_ENABLED) {
            NotificationUtils.sendSuiteSummary(
                    suite.getName(),
                    ReportPublisher.getSummary()
            );
        }
    }

    /* =========================================================
       TEST LEVEL
       ========================================================= */

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);

        log.info("▶ START: {}", testName);

        PerfTimer.start(testName);
        ReportPublisher.startTest(testName);

        result.setAttribute("startTime", System.currentTimeMillis());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);

        long duration = PerfTimer.stop(testName);
        log.info("✔ PASS: {} ({} ms)", testName, duration);

        ReportPublisher.pass(testName, duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);

        Throwable error = result.getThrowable();
        long duration = PerfTimer.stop(testName);

        log.error("✖ FAIL: {} ({} ms)", testName, duration, error);

        // Screenshot
        captureScreenshot(result)
                .ifPresent(path ->
                        ReportPublisher.attachScreenshot(testName, path)
                );

        // Trace / video (if enabled)
        ReportPublisher.attachTrace(testName);

        ReportPublisher.fail(testName, error, duration);

        // Notification
        if (RuntimeConfig.NOTIFICATIONS_ON_FAILURE) {
            NotificationUtils.sendFailureAlert(
                    testName,
                    error.getMessage()
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);

        log.warn("⚠ SKIPPED: {}", testName);
        ReportPublisher.skip(testName);
    }

    /* =========================================================
       RETRY ANALYZER
       ========================================================= */

    @Override
    public boolean retry(ITestResult result) {

        int currentRetry = getRetryCount(result);
        int maxRetry = RuntimeConfig.MAX_RETRY_COUNT;

        if (currentRetry < maxRetry) {
            incrementRetryCount(result);
            log.warn("🔁 Retrying test: {} ({} of {})",
                    getTestName(result),
                    currentRetry + 1,
                    maxRetry);
            return true;
        }
        return false;
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private Optional<String> captureScreenshot(ITestResult result) {
        try {
            Object testInstance = result.getInstance();

            if (testInstance instanceof UsesPlaywrightPage) {
                Page page = ((UsesPlaywrightPage) testInstance).getPage();

                String filePath = ReportPublisher.captureScreenshot(
                        page,
                        getTestName(result)
                );
                return Optional.of(filePath);
            }
        } catch (Exception e) {
            log.error("Failed to capture screenshot", e);
        }
        return Optional.empty();
    }

    private String getTestName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    private int getRetryCount(ITestResult result) {
        return Optional.ofNullable(result.getAttribute("retryCount"))
                .map(Integer.class::cast)
                .orElse(0);
    }

    private void incrementRetryCount(ITestResult result) {
        result.setAttribute(
                "retryCount",
                getRetryCount(result) + 1
        );
    }
}
