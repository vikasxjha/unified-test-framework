package com.company.qa.unified.stepdefs;

import com.company.qa.unified.data.UserFactory;
import com.company.qa.unified.pages.api.AuthApi;
import com.company.qa.unified.pages.api.UserApi;
import com.company.qa.unified.utils.DBAssertions;
import com.company.qa.unified.utils.DBConnector;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthDBSteps
 *
 * Step definitions for Auth API + Database validation scenarios.
 *
 * Validates:
 * - User creation persistence
 * - Auth token generation
 * - Status changes
 * - Duplicate prevention
 * - Invalid credential handling
 */
public class AuthDBSteps {

    private static final Log log = Log.get(AuthDBSteps.class);

    private Response lastResponse;
    private String currentUserEmail;
    private String currentUserId;
    private String correlationId;
    private int initialStatusCode;

    /* =========================================================
       GIVEN
       ========================================================= */

    @Given("the database is accessible")
    public void verifyDatabaseAccessible() {
        log.info("🗄️ Verifying database connectivity");

        // Simple connectivity check
        Object result = DBConnector.queryValue("SELECT 1");
        assertNotNull(result, "Database is not accessible");

        ReportPublisher.step("Database connectivity verified");
    }

    @Given("I clean up any existing test user data")
    public void cleanupExistingTestData() {
        log.info("🧹 Cleaning up existing test data");

        // Clean up test users by email pattern
        DBConnector.execute(
                "DELETE FROM auth_tokens WHERE user_id IN " +
                "(SELECT id FROM users WHERE email LIKE 'test.%@example.com' " +
                "OR email LIKE '%.test@example.com')"
        );

        DBConnector.execute(
                "DELETE FROM users WHERE email LIKE 'test.%@example.com' " +
                "OR email LIKE '%.test@example.com'"
        );

        log.info("Test data cleanup completed");
    }

    @Given("a registered user exists with email {string}")
    public void createRegisteredUser(String email) {
        log.info("👤 Creating registered user: {}", email);

        currentUserEmail = email;

        // Create user via API
        UserFactory.User user = UserFactory.User.builder()
                .email(email)
                .phone("+1" + (4155550000L + System.currentTimeMillis() % 10000))
                .name("Test User")
                .countryCode("US")
                .build();

        Response response = UserApi.createUser(user);

        assertEquals(201, response.getStatusCode(),
                "User creation failed for: " + email);

        currentUserId = response.jsonPath().getString("userId");

        // Verify in database
        DBAssertions.assertRowExists("users", "email", email);

        log.info("User created with ID: {}", currentUserId);
    }

    @Given("the user status in database is {string}")
    public void setUserStatus(String status) {
        log.info("Setting user status to: {}", status);

        DBConnector.execute(
                "UPDATE users SET status = ? WHERE email = ?",
                status, currentUserEmail
        );

        DBAssertions.assertColumnValue(
                "users", "email", currentUserEmail, "status", status
        );
    }

    @Given("I have a unique correlation ID")
    public void generateCorrelationId() {
        correlationId = "CORR-" + UUID.randomUUID().toString();
        log.info("Generated correlation ID: {}", correlationId);
    }

    /* =========================================================
       WHEN
       ========================================================= */

    @When("I create a new user via API with:")
    public void createUserWithDetails(Map<String, String> userData) {
        log.info("🔨 Creating user via API with details: {}", userData);

        currentUserEmail = userData.get("email");

        UserFactory.User user = UserFactory.User.builder()
                .email(userData.get("email"))
                .phone(userData.get("phone"))
                .name(userData.get("name"))
                .countryCode(userData.get("country"))
                .build();

        lastResponse = UserApi.createUser(user);

        if (lastResponse.getStatusCode() == 201) {
            currentUserId = lastResponse.jsonPath().getString("userId");
        }

        ReportPublisher.attachText(
                "User Creation Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I send login request for user {string}")
    public void sendLoginRequest(String email) {
        log.info("🔐 Sending login request for: {}", email);

        currentUserEmail = email;

        UserFactory.User user = UserFactory.User.builder()
                .email(email)
                .phone("+14155551234")
                .build();

        lastResponse = AuthApi.login(user);

        ReportPublisher.attachText(
                "Login Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I attempt to create user with email {string}")
    public void attemptCreateDuplicateUser(String email) {
        log.info("🔄 Attempting to create duplicate user: {}", email);

        UserFactory.User user = UserFactory.User.builder()
                .email(email)
                .phone("+14155559999")
                .name("Duplicate Test")
                .countryCode("US")
                .build();

        lastResponse = UserApi.createUser(user);
    }

    @When("I send login request with invalid credentials:")
    public void sendInvalidLoginRequest(Map<String, String> credentials) {
        log.info("❌ Sending login with invalid credentials");

        currentUserEmail = credentials.get("email");

        UserFactory.User user = UserFactory.User.builder()
                .email(credentials.get("email"))
                .phone("+14155550000")
                .build();

        lastResponse = AuthApi.login(user);
    }

    @When("I update user status to {string} via API")
    public void updateUserStatus(String newStatus) {
        log.info("🔄 Updating user status to: {}", newStatus);

        // Using UserApi to update status
        lastResponse = UserApi.updateUserStatus(currentUserId, newStatus);

        ReportPublisher.attachText(
                "Status Update Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I create a user with missing required field {string}")
    public void createUserWithMissingField(String missingField) {
        log.info("⚠️ Creating user with missing field: {}", missingField);

        UserFactory.User.Builder builder = UserFactory.User.builder()
                .email("incomplete@example.com")
                .phone("+14155551111")
                .name("Incomplete User");

        // Intentionally omit the specified field
        if ("email".equals(missingField)) {
            builder.email(null);
        }

        UserFactory.User user = builder.build();
        lastResponse = UserApi.createUser(user);
    }

    @When("I create user {string} with correlation ID")
    public void createUserWithCorrelationId(String email) {
        log.info("Creating user {} with correlation ID: {}", email, correlationId);

        currentUserEmail = email;

        UserFactory.User user = UserFactory.User.builder()
                .email(email)
                .phone("+14155552222")
                .name("Idempotent User")
                .build();

        lastResponse = UserApi.createUserWithCorrelationId(user, correlationId);
        initialStatusCode = lastResponse.getStatusCode();

        if (lastResponse.getStatusCode() == 201) {
            currentUserId = lastResponse.jsonPath().getString("userId");
        }
    }

    @When("I create user {string} with same correlation ID")
    public void createUserWithSameCorrelationId(String email) {
        log.info("Re-creating user {} with same correlation ID", email);

        UserFactory.User user = UserFactory.User.builder()
                .email(email)
                .phone("+14155552222")
                .name("Idempotent User")
                .build();

        lastResponse = UserApi.createUserWithCorrelationId(user, correlationId);
    }

    /* =========================================================
       THEN
       ========================================================= */

    @Then("the API response status should be {int}")
    public void verifyResponseStatus(int expectedStatus) {
        assertEquals(
                expectedStatus,
                lastResponse.getStatusCode(),
                "API response status mismatch"
        );

        log.info("✅ Verified API status: {}", expectedStatus);
    }

    @Then("the response should contain field {string}")
    public void verifyResponseField(String fieldName) {
        String fieldValue = lastResponse.jsonPath().getString(fieldName);

        assertNotNull(
                fieldValue,
                "Response missing expected field: " + fieldName
        );

        log.info("✅ Verified response contains field: {} = {}", fieldName, fieldValue);
    }

    @Then("the database should contain user with email {string}")
    public void verifyUserExistsInDB(String email) {
        DBAssertions.assertRowExists("users", "email", email);
    }

    @Then("the user status in database should be {string}")
    public void verifyUserStatusInDB(String expectedStatus) {
        DBAssertions.assertColumnValue(
                "users",
                "email",
                currentUserEmail,
                "status",
                expectedStatus
        );
    }

    @Then("the user phone in database should be {string}")
    public void verifyUserPhoneInDB(String expectedPhone) {
        DBAssertions.assertColumnValue(
                "users",
                "email",
                currentUserEmail,
                "phone",
                expectedPhone
        );
    }

    @Then("the database should contain auth token for user {string}")
    public void verifyAuthTokenExists(String email) {
        // Get user ID first
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                email
        );

        assertNotNull(userId, "User not found: " + email);

        // Verify auth token exists
        DBAssertions.assertRowExists("auth_tokens", "user_id", userId);

        log.info("✅ Auth token exists for user: {}", email);
    }

    @Then("the token expiry should be set in the future")
    public void verifyTokenExpiry() {
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                currentUserEmail
        );

        Object expiryObj = DBConnector.queryValue(
                "SELECT expires_at FROM auth_tokens WHERE user_id = ?",
                userId
        );

        assertNotNull(expiryObj, "Token expiry not set");

        // Verify expiry is in the future
        Instant expiry = ((java.sql.Timestamp) expiryObj).toInstant();
        Instant now = Instant.now();

        assertTrue(
                expiry.isAfter(now),
                "Token expiry should be in future, but is: " + expiry
        );

        log.info("✅ Token expiry is set correctly: {}", expiry);
    }

    @Then("the error message should contain {string}")
    public void verifyErrorMessage(String expectedMessage) {
        String responseBody = lastResponse.asString();

        assertTrue(
                responseBody.contains(expectedMessage),
                "Error message should contain: " + expectedMessage
        );
    }

    @Then("the user count in database for {string} should be {int}")
    public void verifyUserCount(String email, int expectedCount) {
        DBAssertions.assertRowCount("users", "email", email, expectedCount);
    }

    @Then("no auth token should exist in database for {string}")
    public void verifyNoAuthToken(String email) {
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                email
        );

        if (userId != null) {
            DBAssertions.assertRowNotExists("auth_tokens", "user_id", userId);
        }

        log.info("✅ Confirmed no auth token for: {}", email);
    }

    @Then("the status change timestamp should be recent")
    public void verifyStatusChangeTimestamp() {
        Object timestamp = DBConnector.queryValue(
                "SELECT updated_at FROM users WHERE email = ?",
                currentUserEmail
        );

        assertNotNull(timestamp, "Updated timestamp not set");

        Instant updated = ((java.sql.Timestamp) timestamp).toInstant();
        Instant now = Instant.now();

        long secondsAgo = now.getEpochSecond() - updated.getEpochSecond();

        assertTrue(
                secondsAgo < 60,
                "Status update should be within last 60 seconds, was " + secondsAgo + " seconds ago"
        );

        log.info("✅ Status change timestamp is recent: {} ({} seconds ago)",
                updated, secondsAgo);
    }

    @Then("no user record should be created in database")
    public void verifyNoUserCreated() {
        // Check that no user was created with the attempted email
        int count = ((Number) DBConnector.queryValue(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                "incomplete@example.com"
        )).intValue();

        assertEquals(0, count, "No user should have been created");
    }

    @Then("both API calls should return status {int}")
    public void verifyBothCallsSuccessful(int expectedStatus) {
        assertEquals(expectedStatus, initialStatusCode,
                "First call status mismatch");
        assertEquals(expectedStatus, lastResponse.getStatusCode(),
                "Second call status mismatch");

        log.info("✅ Both API calls returned status: {}", expectedStatus);
    }

    @Then("only {int} user record should exist in database for {string}")
    public void verifyOnlyOneUserRecord(int expectedCount, String email) {
        DBAssertions.assertRowCount("users", "email", email, expectedCount);

        log.info("✅ Idempotency verified: only {} user(s) created", expectedCount);
    }
}

