package com.company.qa.unified.listeners;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.MailFactory;
import com.company.qa.unified.utils.MonitoringMail;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Listener for test execution events.
 *
 * Responsibilities:
 * - Test failure notifications
 * - Test execution logging
 * - Email alerts on failures
 * - Metrics collection
 *
 * This listener is automatically registered via testng.xml
 */
public class TestExecutionListener implements ITestListener {

    private static final Log log = Log.get(TestExecutionListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("🚀 Test started: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ Test passed: {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ Test failed: {}", result.getName());

        try {
            MonitoringMail mail = MailFactory.create();

            String testName = result.getName();
            String testClass = result.getTestClass().getName();
            String errorMessage = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Unknown error";
            String stackTrace = result.getThrowable() != null
                    ? getStackTraceAsString(result.getThrowable())
                    : "";

            String environment = System.getProperty("env", "QA");
            String buildUrl = System.getenv().getOrDefault("BUILD_URL", "Local");

            String htmlBody = String.format("""
                <html>
                <body>
                    <h2 style="color: red;">❌ Test Failure Alert</h2>
                    <table border="1" cellpadding="10">
                        <tr><td><b>Test Name</b></td><td>%s</td></tr>
                        <tr><td><b>Test Class</b></td><td>%s</td></tr>
                        <tr><td><b>Environment</b></td><td>%s</td></tr>
                        <tr><td><b>Build URL</b></td><td><a href="%s">%s</a></td></tr>
                        <tr><td><b>Error</b></td><td style="color: red;">%s</td></tr>
                    </table>
                    <h3>Stack Trace:</h3>
                    <pre style="background-color: #f4f4f4; padding: 10px;">%s</pre>
                </body>
                </html>
                """,
                testName,
                testClass,
                environment,
                buildUrl,
                buildUrl,
                escapeHtml(errorMessage),
                escapeHtml(stackTrace)
            );

            String from = System.getenv().getOrDefault("MAIL_FROM", "test-automation@company.com");
            String[] to = System.getenv()
                    .getOrDefault("MAIL_TO", "qa-team@company.com")
                    .split(",");

            String subject = String.format("🔴 Test Failure: %s [%s]", testName, environment);

            mail.sendMail(from, to, subject, htmlBody);

            log.info("📧 Failure notification email sent for test: {}", testName);

        } catch (Exception e) {
            log.error("Failed to send failure notification email", e);
            // Don't fail the test because of email sending failure
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭️  Test skipped: {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("⚠️  Test failed but within success percentage: {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("=".repeat(60));
        log.info("🎬 Test Suite Started: {}", context.getName());
        log.info("=".repeat(60));
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("=".repeat(60));
        log.info("🏁 Test Suite Finished: {}", context.getName());
        log.info("Total Tests: {}", context.getAllTestMethods().length);
        log.info("Passed: {}", context.getPassedTests().size());
        log.info("Failed: {}", context.getFailedTests().size());
        log.info("Skipped: {}", context.getSkippedTests().size());
        log.info("=".repeat(60));
    }

    /**
     * Converts throwable to string stack trace.
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        if (throwable.getCause() != null) {
            sb.append("Caused by: ");
            sb.append(getStackTraceAsString(throwable.getCause()));
        }
        return sb.toString();
    }

    /**
     * Escapes HTML special characters.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

