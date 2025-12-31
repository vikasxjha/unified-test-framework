package com.company.qa.unified.stepdefs;

import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.drivers.PlaywrightDriverFactory;
import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.company.qa.unified.utils.NotificationUtils;
import com.company.qa.unified.utils.ScenarioContext;
import com.microsoft.playwright.Page;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.*;

import java.nio.file.Path;

/**
 * Hooks
 *
 * Infrastructure-level Cucumber hooks.
 *
 * Responsibilities:
 * - Driver lifecycle management
 * - Playwright tracing & screenshots
 * - Appium session management
 * - Cleanup & resource safety
 *
 * NOTE:
 * - No assertions here
 * - No test logic here
 * - Pure infra only
 */
public class Hooks {

    private static final Log log =
            Log.get(Hooks.class);

    private Page page;
    private AppiumDriver<?> mobileDriver;

    /* =========================================================
       BEFORE HOOKS
       ========================================================= */

    @Before(order = 0)
    public void beforeScenarioInfra(Scenario scenario) {

        log.info("⚙️ Infra setup for scenario: {}", scenario.getName());

        String platform = RuntimeConfig.getPlatform();

        if (platform.equalsIgnoreCase("web")) {
            setupWeb();
        } else if (platform.equalsIgnoreCase("mobile")) {
            setupMobile();
        }

        ReportPublisher.step("Infrastructure setup completed");
    }

    @Before
    public void beforeScenario(Scenario scenario) {

        boolean enableHealing =
                scenario.getSourceTagNames().contains("@selfHeal");

        ScenarioContext.enableSelfHealing(enableHealing);
    }

    /* =========================================================
       AFTER HOOKS
       ========================================================= */

    @After(order = 1)
    public void afterScenarioFailureHandling(Scenario scenario) {

        if (!scenario.isFailed()) {
            return;
        }

        log.error("📛 Scenario failed, collecting diagnostics");

        try {
            if (page != null) {
                byte[] screenshot =
                        page.screenshot(
                                new com.microsoft.playwright.Page.ScreenshotOptions()
                                        .setFullPage(true)
                        );

                ReportPublisher.attachScreenshot(
                        "Web Failure Screenshot",
                        screenshot
                );
            }

            if (mobileDriver != null) {
                byte[] screenshot =
                        mobileDriver.getScreenshotAs(
                                org.openqa.selenium.OutputType.BYTES
                        );

                ReportPublisher.attachScreenshot(
                        "Mobile Failure Screenshot",
                        screenshot
                );
            }

        } catch (Exception e) {
            log.warn("Failed to capture failure artifacts", e);
        }
    }

    @After(order = 0)
    public void afterScenarioCleanup(Scenario scenario) {

        log.info("🧹 Cleaning up infra for scenario: {}", scenario.getName());

        try {
            teardownWeb();
            teardownMobile();
        } catch (Exception e) {
            log.warn("Cleanup error", e);
        }

        ReportPublisher.step("Infrastructure cleanup completed");
    }

    /* =========================================================
       WEB (PLAYWRIGHT)
       ========================================================= */

    private void setupWeb() {

        log.info("🌐 Starting Playwright session");

        page = PlaywrightDriverFactory.createPage();

        if (RuntimeConfig.isTracingEnabled()) {
            PlaywrightDriverFactory.startTracing();
        }
    }

    private void teardownWeb() {

        if (page == null) {
            return;
        }

        try {
            if (RuntimeConfig.isTracingEnabled()) {
                Path tracePath =
                        PlaywrightDriverFactory.stopTracing();

                ReportPublisher.attachBinary(
                        "Playwright Trace",
                        tracePath.toFile().toString().getBytes(),
                        "application/zip",
                        ".zip"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to stop tracing", e);
        } finally {
            PlaywrightDriverFactory.close();
            page = null;
        }
    }

    /* =========================================================
       MOBILE (APPIUM)
       ========================================================= */

    private void setupMobile() {

        log.info("📱 Starting Appium session");

        mobileDriver =
                AppiumDriverFactory.createDriver();
    }

    private void teardownMobile() {

        if (mobileDriver == null) {
            return;
        }

        try {
            mobileDriver.quit();
        } catch (Exception e) {
            log.warn("Failed to quit Appium driver", e);
        } finally {
            mobileDriver = null;
        }
    }
}
