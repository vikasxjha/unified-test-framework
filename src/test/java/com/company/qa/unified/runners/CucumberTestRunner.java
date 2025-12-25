package com.company.qa.unified.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * CucumberTestRunner
 *
 * Master Cucumber runner for the Unified Automation Framework.
 *
 * Supports:
 * - Web (Playwright)
 * - Mobile (Appium)
 * - API
 * - Events
 * - Security
 * - Performance
 * - Chaos tests
 *
 * Execution:
 * - Tag-driven
 * - Parallel-ready
 * - CI-safe
 */
@CucumberOptions(
        features = {
                "src/test/java/features"
        },
        glue = {
                "com.company.qa.unified.stepdefs",
                "com.company.qa.unified.listeners"
        },
        plugin = {
                "pretty",
                "summary",
                "html:reports/cucumber/cucumber.html",
                "json:reports/cucumber/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false,
        tags = "not @Ignore"
)
public class CucumberTestRunner
        extends AbstractTestNGCucumberTests {

    /**
     * Enables parallel execution of scenarios.
     *
     * Controlled via:
     * - testng.xml
     * - Maven Surefire/Failsafe
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
