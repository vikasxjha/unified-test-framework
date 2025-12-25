package com.company.qa.unified.stepdefs;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.company.qa.unified.utils.NotificationUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * CommonSteps
 *
 * Cross-cutting Cucumber steps and hooks used by:
 * - Web
 * - Mobile
 * - API
 * - Security
 * - Performance
 * - Chaos tests
 *
 * Responsibilities:
 * - Scenario lifecycle logging
 * - Environment awareness
 * - Failure notifications
 * - Shared assertions
 */
public class CommonSteps {

    private static final Log log =
            Log.get(CommonSteps.class);

    private Scenario scenario;

    /* =========================================================
       CUCUMBER HOOKS
       ========================================================= */

    @Before
    public void beforeScenario(Scenario scenario) {

        this.scenario = scenario;

        String env = RuntimeConfig.getEnvironment();
        String browser = RuntimeConfig.getBrowser();

        log.info("🚀 Starting Scenario: {}", scenario.getName());
        log.info("🌍 Environment: {}", env);
        log.info("🧪 Tags: {}", scenario.getSourceTagNames());

        ReportPublisher.step(
                "Scenario started: " + scenario.getName()
        );

        ReportPublisher.attachText(
                "Execution Context",
                String.format(
                        "Scenario: %s\nEnvironment: %s\nBrowser: %s\nTags: %s",
                        scenario.getName(),
                        env,
                        browser,
                        scenario.getSourceTagNames()
                )
        );
    }

    @After
    public void afterScenario(Scenario scenario) {

        if (scenario.isFailed()) {

            Throwable error =
                    (Throwable) scenario.getError();

            log.error("❌ Scenario FAILED: {}", scenario.getName());

            ReportPublisher.publishFailure(
                    "Scenario failed: " + scenario.getName(),
                    error
            );

            NotificationUtils.notifyTestFailure(
                    scenario.getName(),
                    RuntimeConfig.getEnvironment(),
                    error
            );

        } else {

            log.info("✅ Scenario PASSED: {}", scenario.getName());

            NotificationUtils.notifyTestSuccess(
                    scenario.getName(),
                    RuntimeConfig.getEnvironment()
            );
        }

        ReportPublisher.step(
                "Scenario finished: " + scenario.getName()
        );
    }

    /* =========================================================
       GENERIC STEPS (REUSABLE)
       ========================================================= */

    /**
     * Used in feature files to explicitly mark logical checkpoints.
     *
     * Example:
     *   And checkpoint "User is authenticated"
     */
    @io.cucumber.java.en.And("checkpoint {string}")
    public void checkpoint(String message) {

        log.info("🔖 Checkpoint: {}", message);
        ReportPublisher.step("Checkpoint: " + message);
    }

    /**
     * Used to log test metadata dynamically.
     *
     * Example:
     *   And log "Using premium test user"
     */
    @io.cucumber.java.en.And("log {string}")
    public void logMessage(String message) {

        log.info("📝 {}", message);
        ReportPublisher.attachText("Log", message);
    }

    /**
     * Environment assertion.
     *
     * Example:
     *   Given I am running in "qa" environment
     */
    @io.cucumber.java.en.Given("I am running in {string} environment")
    public void assertEnvironment(String expectedEnv) {

        String actualEnv =
                RuntimeConfig.getEnvironment();

        if (!expectedEnv.equalsIgnoreCase(actualEnv)) {
            throw new AssertionError(
                    "Expected env=" + expectedEnv +
                            " but was=" + actualEnv
            );
        }

        ReportPublisher.step(
                "Verified environment: " + actualEnv
        );
    }

    /**
     * Feature flag assertion helper.
     *
     * Example:
     *   And feature flag "new_search" should be enabled
     */
    @io.cucumber.java.en.And(
            "feature flag {string} should be enabled"
    )
    public void assertFeatureFlagEnabled(String flag) {

        boolean enabled =
                EnvironmentConfig
                        .getFeatureFlags()
                        .isEnabled(flag);

        if (!enabled) {
            throw new AssertionError(
                    "Feature flag disabled: " + flag
            );
        }

        ReportPublisher.step(
                "Feature flag enabled: " + flag
        );
    }
}
