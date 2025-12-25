package com.company.qa.unified.stepdefs;

import com.company.qa.unified.pages.api.SearchApi;
import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.drivers.APIClientFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiSteps
 *
 * Step definitions for API scenarios.
 * Covers:
 * - Health checks
 * - Core API availability
 * - Basic functional smoke validation
 */
public class ApiSteps {

    private static final Log log =
            Log.get(ApiSteps.class);

    private Response lastResponse;

    private SearchApi searchApi;

    /* =========================================================
       GIVEN
       ========================================================= */

    @Given("the Auth API is available")
    public void authApiIsAvailable() {

        String baseUrl = EnvironmentConfig.get().getApiBaseUrl();

        log.info("🔌 Auth API base URL: {}", baseUrl);

        ReportPublisher.step("Auth API is available");
    }

    @Given("the Search API is available")
    public void searchApiIsAvailable() {

        String baseUrl = EnvironmentConfig.get().getApiBaseUrl();
        searchApi = new SearchApi();

        log.info("🔌 Search API base URL: {}", baseUrl);

        ReportPublisher.step("Search API is available");
    }

    /* =========================================================
       WHEN
       ========================================================= */

    @When("I send a health check request to Auth API")
    public void sendHealthCheckToAuthApi() {

        log.info("➡️ Sending Auth API health check");

        // Simple health check using a GET request to the API base URL
        lastResponse = APIClientFactory.defaultClient()
                .get("/users/1"); // Using JSONPlaceholder endpoint

        ReportPublisher.attachText(
                "Auth API Health Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I search for phone number {string}")
    public void searchForPhoneNumber(String phoneNumber) {

        log.info("➡️ Searching phone number via Search API: {}", phoneNumber);

        // Using anonymous search as a simple search endpoint
        lastResponse = searchApi.anonymousSearch(phoneNumber, 10);

        ReportPublisher.attachText(
                "Search API Response",
                lastResponse.asPrettyString()
        );
    }

    /* =========================================================
       THEN
       ========================================================= */

    @Then("the API response status should be {int}")
    public void verifyApiResponseStatus(int expectedStatusCode) {

        int actualStatusCode = lastResponse.getStatusCode();

        log.info(
                "✅ API response status = {} (expected {})",
                actualStatusCode,
                expectedStatusCode
        );

        ReportPublisher.step(
                "Verified API response status: " + actualStatusCode
        );

        assertEquals(
                expectedStatusCode,
                actualStatusCode,
                "API response status code mismatch"
        );
    }

    @Then("the response time should be under {int} milliseconds")
    public void verifyResponseTimeUnder(int maxMilliseconds) {

        long actualResponseTime = lastResponse.getTime();

        log.info(
                "⚡ API response time = {}ms (threshold = {}ms)",
                actualResponseTime,
                maxMilliseconds
        );

        ReportPublisher.step(
                String.format(
                        "Verified API response time: %dms (threshold: %dms)",
                        actualResponseTime,
                        maxMilliseconds
                )
        );

        ReportPublisher.attachText(
                "Performance Metrics",
                String.format(
                        "Response Time: %dms%nThreshold: %dms%nStatus: %s",
                        actualResponseTime,
                        maxMilliseconds,
                        actualResponseTime <= maxMilliseconds ? "PASSED" : "FAILED"
                )
        );

        assertTrue(
                actualResponseTime <= maxMilliseconds,
                String.format(
                        "API response time %dms exceeded threshold of %dms",
                        actualResponseTime,
                        maxMilliseconds
                )
        );
    }
}
