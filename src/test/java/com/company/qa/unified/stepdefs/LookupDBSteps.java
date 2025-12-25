package com.company.qa.unified.stepdefs;

import com.company.qa.unified.data.UserFactory;
import com.company.qa.unified.pages.api.PhoneLookupApi;
import com.company.qa.unified.utils.DBAssertions;
import com.company.qa.unified.utils.DBConnector;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LookupDBSteps
 *
 * Step definitions for Phone Lookup API + Database validation.
 *
 * Validates:
 * - Lookup history persistence
 * - Hit counter increments
 * - Anonymous lookups
 * - Spam flag handling
 * - Rate limiting
 */
public class LookupDBSteps {

    private static final Log log = Log.get(LookupDBSteps.class);

    private Response lastResponse;
    private String currentUserEmail;
    private String currentUserId;
    private String currentPhoneNumber;
    private List<Response> concurrentResponses = new ArrayList<>();

    /* =========================================================
       GIVEN
       ========================================================= */

    @Given("I have a registered user with email {string}")
    public void setupRegisteredUser(String email) {
        log.info("Setting up registered user: {}", email);

        currentUserEmail = email;

        // Get or create user
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                email
        );

        if (userId == null) {
            // Create user
            UserFactory.User user = UserFactory.User.builder()
                    .email(email)
                    .phone("+14155551000")
                    .name("Lookup Test User")
                    .build();

            DBConnector.execute(
                    "INSERT INTO users (email, phone, name, status) VALUES (?, ?, ?, ?)",
                    email, user.phone(), user.name(), "ACTIVE"
            );

            userId = DBConnector.queryValue(
                    "SELECT id FROM users WHERE email = ?",
                    email
            );
        }

        currentUserId = userId.toString();
        log.info("User setup complete with ID: {}", currentUserId);
    }

    @Given("I have performed lookup for number {string}")
    public void performInitialLookup(String phoneNumber) {
        log.info("Performing initial lookup for: {}", phoneNumber);

        currentPhoneNumber = phoneNumber;

        PhoneLookupApi.lookupPhone(
                phoneNumber,
                "mock-token",
                true
        );

        // Verify in DB
        DBAssertions.assertRowExists("lookup_history", "phone_number", phoneNumber);
    }

    @Given("the lookup count in database is {int}")
    public void verifyInitialLookupCount(int expectedCount) {
        DBAssertions.assertColumnValue(
                "lookup_history",
                "phone_number",
                currentPhoneNumber,
                "lookup_count",
                expectedCount
        );
    }

    @Given("phone number {string} is marked as spam")
    public void markPhoneAsSpam(String phoneNumber) {
        log.info("Marking phone as spam: {}", phoneNumber);

        currentPhoneNumber = phoneNumber;

        DBConnector.execute(
                "INSERT INTO phone_metadata (phone_number, is_spam, spam_score) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (phone_number) DO UPDATE SET is_spam = TRUE, spam_score = 95",
                phoneNumber, true, 95
        );
    }

    @Given("phone number {string} has lookup count {int}")
    public void setLookupCount(String phoneNumber, int count) {
        currentPhoneNumber = phoneNumber;

        if (count == 0) {
            // Ensure no existing record
            DBConnector.execute(
                    "DELETE FROM lookup_history WHERE phone_number = ?",
                    phoneNumber
            );
        } else {
            // Set specific count
            DBConnector.execute(
                    "INSERT INTO lookup_history (phone_number, user_id, lookup_count) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT (phone_number, user_id) DO UPDATE SET lookup_count = ?",
                    phoneNumber, currentUserId, count, count
            );
        }
    }

    /* =========================================================
       WHEN
       ========================================================= */

    @When("I perform phone lookup for number {string}")
    public void performPhoneLookup(String phoneNumber) {
        log.info("🔍 Performing phone lookup: {}", phoneNumber);

        currentPhoneNumber = phoneNumber;

        lastResponse = PhoneLookupApi.lookupPhone(
                phoneNumber,
                "mock-token-" + currentUserId,
                true
        );

        ReportPublisher.attachText(
                "Lookup Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I perform lookup for number {string} again")
    public void performRepeatedLookup(String phoneNumber) {
        log.info("🔄 Performing repeated lookup: {}", phoneNumber);

        lastResponse = PhoneLookupApi.lookupPhone(
                phoneNumber,
                "mock-token-" + currentUserId,
                true
        );
    }

    @When("I perform lookup for invalid number {string}")
    public void performInvalidLookup(String invalidNumber) {
        log.info("❌ Performing lookup for invalid number: {}", invalidNumber);

        currentPhoneNumber = invalidNumber;

        lastResponse = PhoneLookupApi.lookupPhone(
                invalidNumber,
                "mock-token",
                true
        );
    }

    @When("I perform anonymous lookup for number {string}")
    public void performAnonymousLookup(String phoneNumber) {
        log.info("👤 Performing anonymous lookup: {}", phoneNumber);

        currentPhoneNumber = phoneNumber;

        lastResponse = PhoneLookupApi.anonymousLookup(phoneNumber);

        ReportPublisher.attachText(
                "Anonymous Lookup Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I perform {int} concurrent lookups for {string}")
    public void performConcurrentLookups(int count, String phoneNumber) {
        log.info("⚡ Performing {} concurrent lookups for: {}", count, phoneNumber);

        currentPhoneNumber = phoneNumber;
        concurrentResponses.clear();

        ExecutorService executor = Executors.newFixedThreadPool(count);
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
                return PhoneLookupApi.lookupPhone(
                        phoneNumber,
                        "mock-token-" + currentUserId,
                        true
                );
            }, executor);

            futures.add(future);
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        futures.forEach(f -> {
            try {
                concurrentResponses.add(f.get());
            } catch (Exception e) {
                log.error("Concurrent lookup failed", e);
            }
        });

        executor.shutdown();

        log.info("Completed {} concurrent lookups", concurrentResponses.size());
    }

    @When("I perform {int} lookups within {int} minute")
    public void performRateLimitTest(int lookupCount, int minutes) {
        log.info("Performing {} lookups for rate limit test", lookupCount);

        for (int i = 0; i < lookupCount; i++) {
            lastResponse = PhoneLookupApi.lookupPhone(
                    "+14155559999",
                    "mock-token-" + currentUserId,
                    true
            );

            if (lastResponse.getStatusCode() == 429) {
                log.info("Rate limit hit after {} lookups", i + 1);
                break;
            }
        }
    }

    @When("I perform enriched lookup for {string}")
    public void performEnrichedLookup(String phoneNumber) {
        log.info("📊 Performing enriched lookup: {}", phoneNumber);

        currentPhoneNumber = phoneNumber;

        lastResponse = PhoneLookupApi.enrichedLookup(
                phoneNumber,
                "mock-token-" + currentUserId
        );
    }

    /* =========================================================
       THEN
       ========================================================= */

    @Then("the database should contain lookup history for number {string}")
    public void verifyLookupHistory(String phoneNumber) {
        DBAssertions.assertRowExists("lookup_history", "phone_number", phoneNumber);
    }

    @Then("the lookup history should be linked to user {string}")
    public void verifyUserLinkage(String email) {
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                email
        );

        assertNotNull(userId, "User not found: " + email);

        Object linkedUserId = DBConnector.queryValue(
                "SELECT user_id FROM lookup_history WHERE phone_number = ?",
                currentPhoneNumber
        );

        assertEquals(
                userId.toString(),
                linkedUserId.toString(),
                "Lookup not linked to correct user"
        );

        log.info("✅ Lookup linked to user: {}", email);
    }

    @Then("the lookup timestamp should be recent")
    public void verifyLookupTimestamp() {
        Object timestamp = DBConnector.queryValue(
                "SELECT last_lookup_at FROM lookup_history WHERE phone_number = ?",
                currentPhoneNumber
        );

        assertNotNull(timestamp, "Lookup timestamp not set");

        Instant lookupTime = ((java.sql.Timestamp) timestamp).toInstant();
        Instant now = Instant.now();

        long secondsAgo = now.getEpochSecond() - lookupTime.getEpochSecond();

        assertTrue(
                secondsAgo < 60,
                "Lookup should be within last 60 seconds, was " + secondsAgo + " seconds ago"
        );
    }

    @Then("the lookup count in database should be {int}")
    public void verifyLookupCount(int expectedCount) {
        DBAssertions.assertColumnValue(
                "lookup_history",
                "phone_number",
                currentPhoneNumber,
                "lookup_count",
                expectedCount
        );
    }

    @Then("the last lookup timestamp should be updated")
    public void verifyTimestampUpdated() {
        verifyLookupTimestamp(); // Reuse existing verification
    }

    @Then("no lookup history should exist for {string}")
    public void verifyNoLookupHistory(String phoneNumber) {
        DBAssertions.assertRowNotExists("lookup_history", "phone_number", phoneNumber);
    }

    @Then("the response should indicate spam status")
    public void verifySpamStatusInResponse() {
        Boolean isSpam = lastResponse.jsonPath().getBoolean("isSpam");

        assertTrue(isSpam, "Response should indicate spam status");

        log.info("✅ Response correctly indicates spam status");
    }

    @Then("the database should show spam flag TRUE for {string}")
    public void verifySpamFlag(String phoneNumber) {
        DBAssertions.assertColumnValue(
                "phone_metadata",
                "phone_number",
                phoneNumber,
                "is_spam",
                true
        );
    }

    @Then("the lookup history user_id should be NULL")
    public void verifyAnonymousLookup() {
        DBAssertions.assertColumnNull(
                "lookup_history",
                "phone_number",
                currentPhoneNumber,
                "user_id"
        );
    }

    @Then("all API responses should be successful")
    public void verifyAllResponsesSuccessful() {
        for (Response response : concurrentResponses) {
            assertTrue(
                    response.getStatusCode() >= 200 && response.getStatusCode() < 300,
                    "Response status should be 2xx, got: " + response.getStatusCode()
            );
        }

        log.info("✅ All {} responses were successful", concurrentResponses.size());
    }

    @Then("the last lookup should be rate-limited")
    public void verifyRateLimited() {
        assertEquals(429, lastResponse.getStatusCode(),
                "Expected rate limit status 429");
    }

    @Then("the rate limit counter in database should reflect attempts")
    public void verifyRateLimitCounter() {
        DBAssertions.assertColumnGreaterThan(
                "rate_limits",
                "user_id",
                currentUserId,
                "request_count",
                90
        );
    }

    @Then("the API response should contain carrier information")
    public void verifyCarrierInfo() {
        String carrier = lastResponse.jsonPath().getString("carrier");
        assertNotNull(carrier, "Carrier information missing");
    }

    @Then("the database should persist carrier data for {string}")
    public void verifyCarrierDataPersisted(String phoneNumber) {
        DBAssertions.assertColumnNotNull(
                "phone_metadata",
                "phone_number",
                phoneNumber,
                "carrier_name"
        );
    }

    @Then("the database should persist location data")
    public void verifyLocationDataPersisted() {
        DBAssertions.assertColumnNotNull(
                "phone_metadata",
                "phone_number",
                currentPhoneNumber,
                "location_region"
        );
    }

    @Then("the enrichment timestamp should be set")
    public void verifyEnrichmentTimestamp() {
        DBAssertions.assertColumnNotNull(
                "phone_metadata",
                "phone_number",
                currentPhoneNumber,
                "enriched_at"
        );
    }
}

