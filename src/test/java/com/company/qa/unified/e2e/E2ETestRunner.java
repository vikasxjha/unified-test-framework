package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.DBConnector;
import com.company.qa.unified.utils.Log;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

/**
 * E2ETestRunner
 *
 * TestNG runner for API + Database E2E test scenarios.
 *
 * Features:
 * - Parallel test execution
 * - Database connection management
 * - Allure reporting integration
 * - Comprehensive cleanup
 *
 * Run with:
 *   mvn test -Dcucumber.filter.tags="@api and @db"
 *   mvn test -Dcucumber.filter.tags="@e2e"
 */
@CucumberOptions(
        features = "src/test/resources/features/api_db",
        glue = {"com.company.qa.unified.stepdefs"},
        tags = "@api and @db",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false
)
public class E2ETestRunner extends AbstractTestNGCucumberTests {

    private static final Log log = Log.get(E2ETestRunner.class);

    @BeforeClass
    public void setupE2ETests() {
        log.info("=".repeat(60));
        log.info("🚀 Starting E2E API + Database Test Suite");
        log.info("=".repeat(60));

        // Verify database connectivity
        try {
            Object result = DBConnector.queryValue("SELECT 1");
            log.info("✅ Database connectivity verified");
        } catch (Exception e) {
            log.error("❌ Database connection failed", e);
            throw new RuntimeException("Cannot proceed without database", e);
        }
    }

    @AfterClass
    public void teardownE2ETests() {
        log.info("=".repeat(60));
        log.info("🧹 Cleaning up E2E test resources");
        log.info("=".repeat(60));

        // Close database connections
        DBConnector.close();

        log.info("✅ E2E test suite completed");
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}

