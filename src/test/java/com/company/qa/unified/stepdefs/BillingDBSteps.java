package com.company.qa.unified.stepdefs;

import com.company.qa.unified.data.PaymentDataFactory;
import com.company.qa.unified.pages.api.BillingApi;
import com.company.qa.unified.pages.api.SubscriptionApi;
import com.company.qa.unified.utils.DBAssertions;
import com.company.qa.unified.utils.DBConnector;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BillingDBSteps
 *
 * Step definitions for Billing/Subscription API + Database validation.
 *
 * Validates:
 * - Transaction creation and status
 * - Subscription lifecycle
 * - Payment success/failure handling
 * - Refund processing
 * - Idempotency
 */
public class BillingDBSteps {

    private static final Log log = Log.get(BillingDBSteps.class);

    private Response lastResponse;
    private String currentUserId;
    private String currentCheckoutId;
    private String currentTransactionId;
    private String currentSubscriptionId;
    private BigDecimal currentAmount;
    private List<Integer> concurrentStatusCodes = new ArrayList<>();

    /* =========================================================
       GIVEN
       ========================================================= */

    @Given("I have a pending checkout with ID {string}")
    public void createPendingCheckout(String checkoutId) {
        log.info("Creating pending checkout: {}", checkoutId);

        currentCheckoutId = checkoutId;

        // Get or use existing user
        Object userId = DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                "billing.user@example.com"
        );

        if (userId == null) {
            userId = createTestUser("billing.user@example.com");
        }

        currentUserId = userId.toString();

        // Create pending checkout in DB
        DBConnector.execute(
                "INSERT INTO transactions (checkout_id, user_id, amount, currency, status) " +
                "VALUES (?, ?, ?, ?, ?)",
                checkoutId, currentUserId, 99.99, "USD", "PENDING"
        );

        currentTransactionId = DBConnector.queryValue(
                "SELECT id FROM transactions WHERE checkout_id = ?",
                checkoutId
        ).toString();
    }

    @Given("I have an active subscription that expires today")
    public void createExpiringSubscription() {
        log.info("Creating expiring subscription");

        // Create subscription expiring today
        DBConnector.execute(
                "INSERT INTO subscriptions (user_id, plan_type, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?)",
                currentUserId, "PREMIUM_MONTHLY", "ACTIVE",
                LocalDate.now().minusDays(30), LocalDate.now()
        );

        currentSubscriptionId = DBConnector.queryValue(
                "SELECT id FROM subscriptions WHERE user_id = ? AND status = ?",
                currentUserId, "ACTIVE"
        ).toString();
    }

    @Given("I have an active {string} subscription")
    public void createActiveSubscription(String planType) {
        log.info("Creating active subscription: {}", planType);

        DBConnector.execute(
                "INSERT INTO subscriptions (user_id, plan_type, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?)",
                currentUserId, planType, "ACTIVE",
                LocalDate.now(), LocalDate.now().plusDays(30)
        );

        currentSubscriptionId = DBConnector.queryValue(
                "SELECT id FROM subscriptions WHERE user_id = ? AND status = ?",
                currentUserId, "ACTIVE"
        ).toString();
    }

    @Given("I have a completed transaction {string}")
    public void createCompletedTransaction(String transactionId) {
        log.info("Creating completed transaction: {}", transactionId);

        currentTransactionId = transactionId;

        DBConnector.execute(
                "INSERT INTO transactions (id, user_id, amount, currency, status) " +
                "VALUES (?, ?, ?, ?, ?)",
                transactionId, currentUserId, 99.99, "USD", "COMPLETED"
        );
    }

    @Given("I have a completed payment of ${double}")
    public void createCompletedPayment(double amount) {
        log.info("Creating completed payment of: ${}", amount);

        currentAmount = BigDecimal.valueOf(amount);

        DBConnector.execute(
                "INSERT INTO transactions (user_id, amount, currency, status) " +
                "VALUES (?, ?, ?, ?)",
                currentUserId, amount, "USD", "COMPLETED"
        );

        currentTransactionId = DBConnector.queryValue(
                "SELECT id FROM transactions WHERE user_id = ? AND status = ? ORDER BY created_at DESC LIMIT 1",
                currentUserId, "COMPLETED"
        ).toString();
    }

    @Given("I have an active subscription ending in {int} days")
    public void createSubscriptionEndingInDays(int days) {
        log.info("Creating subscription ending in {} days", days);

        DBConnector.execute(
                "INSERT INTO subscriptions (user_id, plan_type, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?)",
                currentUserId, "PREMIUM_MONTHLY", "ACTIVE",
                LocalDate.now().minusDays(27), LocalDate.now().plusDays(days)
        );

        currentSubscriptionId = DBConnector.queryValue(
                "SELECT id FROM subscriptions WHERE user_id = ? AND status = ?",
                currentUserId, "ACTIVE"
        ).toString();
    }

    /* =========================================================
       WHEN
       ========================================================= */

    @When("I create checkout for plan {string}")
    public void createCheckout(String planType) {
        log.info("💳 Creating checkout for plan: {}", planType);

        PaymentDataFactory.PaymentRequest payment =
                PaymentDataFactory.createSubscriptionPayment(planType, "USD");

        lastResponse = BillingApi.createCheckout(currentUserId, payment);

        if (lastResponse.getStatusCode() == 201) {
            currentCheckoutId = lastResponse.jsonPath().getString("checkoutId");
            currentAmount = new BigDecimal(
                    lastResponse.jsonPath().getString("amount")
            );
        }

        ReportPublisher.attachText(
                "Checkout Response",
                lastResponse.asPrettyString()
        );
    }

    @When("I process payment successfully for checkout {string}")
    public void processPaymentSuccess(String checkoutId) {
        log.info("✅ Processing successful payment for: {}", checkoutId);

        lastResponse = BillingApi.processPayment(
                checkoutId,
                "tok_visa", // Mock success token
                true
        );
    }

    @When("I process payment with failure for checkout {string}")
    public void processPaymentFailure(String checkoutId) {
        log.info("❌ Processing failed payment for: {}", checkoutId);

        lastResponse = BillingApi.processPayment(
                checkoutId,
                "tok_chargeDeclined", // Mock failure token
                false
        );
    }

    @When("the subscription expiry job runs")
    public void runExpiryJob() {
        log.info("⏰ Running subscription expiry job");

        // Simulate expiry job by updating expired subscriptions
        DBConnector.execute(
                "UPDATE subscriptions SET status = ?, expired_at = CURRENT_TIMESTAMP " +
                "WHERE end_date <= CURRENT_DATE AND status = ?",
                "EXPIRED", "ACTIVE"
        );
    }

    @When("I upgrade to plan {string}")
    public void upgradePlan(String newPlan) {
        log.info("⬆️ Upgrading to plan: {}", newPlan);

        lastResponse = SubscriptionApi.upgrade(currentSubscriptionId, newPlan);
    }

    @When("I attempt to process payment for {string} again")
    public void processPaymentAgain(String transactionId) {
        log.info("🔄 Attempting to reprocess transaction: {}", transactionId);

        lastResponse = BillingApi.processPaymentByTransactionId(
                transactionId,
                "tok_visa",
                true
        );
    }

    @When("I process partial refund of ${double}")
    public void processPartialRefund(double refundAmount) {
        log.info("💰 Processing partial refund of: ${}", refundAmount);

        lastResponse = BillingApi.processRefund(
                currentTransactionId,
                BigDecimal.valueOf(refundAmount),
                "PARTIAL",
                "Customer request"
        );
    }

    @When("I attempt {int} concurrent subscription purchases")
    public void attemptConcurrentPurchases(int count) {
        log.info("⚡ Attempting {} concurrent subscription purchases", count);

        concurrentStatusCodes.clear();

        ExecutorService executor = Executors.newFixedThreadPool(count);
        List<CompletableFuture<Response>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
                PaymentDataFactory.PaymentRequest payment =
                        PaymentDataFactory.createSubscriptionPayment("PREMIUM_MONTHLY", "USD");
                return BillingApi.createCheckout(currentUserId, payment);
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        futures.forEach(f -> {
            try {
                concurrentStatusCodes.add(f.get().getStatusCode());
            } catch (Exception e) {
                log.error("Concurrent purchase failed", e);
            }
        });

        executor.shutdown();
    }

    @When("I create checkout with amount {int}")
    public void createCheckoutWithAmount(int amount) {
        log.info("Creating checkout with amount: {}", amount);

        PaymentDataFactory.PaymentRequest payment =
                PaymentDataFactory.PaymentRequest.builder()
                .amount(BigDecimal.valueOf(amount))
                .currency("USD")
                .planType("CUSTOM")
                .build();

        lastResponse = BillingApi.createCheckout(currentUserId, payment);
    }

    @When("I renew subscription via API")
    public void renewSubscription() {
        log.info("🔄 Renewing subscription");

        lastResponse = SubscriptionApi.renewSubscription(currentSubscriptionId);
    }

    /* =========================================================
       THEN
       ========================================================= */

    @Then("the database should contain transaction for checkout ID")
    public void verifyTransactionExists() {
        DBAssertions.assertRowExists("transactions", "checkout_id", currentCheckoutId);
    }

    @Then("the transaction status should be {string}")
    public void verifyTransactionStatus(String expectedStatus) {
        DBAssertions.assertColumnValue(
                "transactions",
                "checkout_id",
                currentCheckoutId,
                "status",
                expectedStatus
        );
    }

    @Then("the transaction amount should match API response")
    public void verifyTransactionAmount() {
        BigDecimal dbAmount = (BigDecimal) DBConnector.queryValue(
                "SELECT amount FROM transactions WHERE checkout_id = ?",
                currentCheckoutId
        );

        assertEquals(
                currentAmount.doubleValue(),
                dbAmount.doubleValue(),
                0.01,
                "Transaction amount mismatch"
        );
    }

    @Then("the transaction status in database should be {string}")
    public void verifyTransactionStatusById(String expectedStatus) {
        DBAssertions.assertColumnValue(
                "transactions",
                "id",
                currentTransactionId,
                "status",
                expectedStatus
        );
    }

    @Then("the subscription status in database should be {string}")
    public void verifySubscriptionStatus(String expectedStatus) {
        Object subId = DBConnector.queryValue(
                "SELECT id FROM subscriptions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                currentUserId
        );

        DBAssertions.assertColumnValue(
                "subscriptions",
                "id",
                subId,
                "status",
                expectedStatus
        );
    }

    @Then("the subscription start date should be today")
    public void verifySubscriptionStartDate() {
        Object startDate = DBConnector.queryValue(
                "SELECT start_date FROM subscriptions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                currentUserId
        );

        LocalDate start = ((java.sql.Date) startDate).toLocalDate();
        LocalDate today = LocalDate.now();

        assertEquals(today, start, "Subscription start date should be today");
    }

    @Then("the subscription end date should be {int} days from now")
    public void verifySubscriptionEndDate(int days) {
        Object endDate = DBConnector.queryValue(
                "SELECT end_date FROM subscriptions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                currentUserId
        );

        LocalDate end = ((java.sql.Date) endDate).toLocalDate();
        LocalDate expected = LocalDate.now().plusDays(days);

        long daysDiff = ChronoUnit.DAYS.between(end, expected);

        assertTrue(
                Math.abs(daysDiff) <= 1,
                "Subscription end date should be " + days + " days from now, diff: " + daysDiff
        );
    }

    @Then("no active subscription should exist in database")
    public void verifyNoActiveSubscription() {
        Integer count = ((Number) DBConnector.queryValue(
                "SELECT COUNT(*) FROM subscriptions WHERE user_id = ? AND status = ?",
                currentUserId, "ACTIVE"
        )).intValue();

        assertEquals(0, count, "No active subscription should exist");
    }

    @Then("the expiry timestamp should be set")
    public void verifyExpiryTimestamp() {
        DBAssertions.assertColumnNotNull(
                "subscriptions",
                "id",
                currentSubscriptionId,
                "expired_at"
        );
    }

    @Then("the user should have no active subscription")
    public void verifyUserHasNoActiveSubscription() {
        verifyNoActiveSubscription();
    }

    @Then("the old subscription status should be {string}")
    public void verifyOldSubscriptionStatus(String expectedStatus) {
        DBAssertions.assertColumnValue(
                "subscriptions",
                "id",
                currentSubscriptionId,
                "status",
                expectedStatus
        );
    }

    @Then("the new subscription status should be {string}")
    public void verifyNewSubscriptionStatus(String expectedStatus) {
        Object newSubId = DBConnector.queryValue(
                "SELECT id FROM subscriptions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1",
                currentUserId
        );

        DBAssertions.assertColumnValue(
                "subscriptions",
                "id",
                newSubId,
                "status",
                expectedStatus
        );
    }

    @Then("the closure reason should be {string}")
    public void verifyClosureReason(String expectedReason) {
        DBAssertions.assertColumnValue(
                "subscriptions",
                "id",
                currentSubscriptionId,
                "closure_reason",
                expectedReason
        );
    }

    @Then("the transaction status should remain {string}")
    public void verifyTransactionStatusRemains(String expectedStatus) {
        verifyTransactionStatusById(expectedStatus);
    }

    @Then("no duplicate subscription should be created")
    public void verifyNoDuplicateSubscription() {
        Integer count = ((Number) DBConnector.queryValue(
                "SELECT COUNT(*) FROM subscriptions WHERE user_id = ? AND status = ?",
                currentUserId, "ACTIVE"
        )).intValue();

        assertTrue(count <= 1, "Should have at most 1 active subscription");
    }

    @Then("the refund record should exist in database")
    public void verifyRefundRecord() {
        DBAssertions.assertRowExists("refunds", "transaction_id", currentTransactionId);
    }

    @Then("the refund amount should be ${double}")
    public void verifyRefundAmount(double expectedAmount) {
        BigDecimal dbAmount = (BigDecimal) DBConnector.queryValue(
                "SELECT amount FROM refunds WHERE transaction_id = ?",
                currentTransactionId
        );

        assertEquals(
                expectedAmount,
                dbAmount.doubleValue(),
                0.01,
                "Refund amount mismatch"
        );
    }

    @Then("the original transaction should be marked {string}")
    public void verifyTransactionMarked(String expectedStatus) {
        verifyTransactionStatusById(expectedStatus);
    }

    @Then("only {int} purchase should succeed")
    public void verifyOnlyOnePurchaseSucceeded(int expectedSuccess) {
        long successCount = concurrentStatusCodes.stream()
                .filter(code -> code >= 200 && code < 300)
                .count();

        assertEquals(expectedSuccess, successCount,
                "Only " + expectedSuccess + " purchase should succeed");
    }

    @Then("only {int} active subscription should exist in database")
    public void verifyActiveSubscriptionCount(int expectedCount) {
        DBAssertions.assertRowCount(
                "subscriptions",
                "user_id",
                currentUserId,
                expectedCount
        );
    }

    @Then("no transaction should be created in database")
    public void verifyNoTransaction() {
        // Check that no transaction was created for current user in last minute
        Integer count = ((Number) DBConnector.queryValue(
                "SELECT COUNT(*) FROM transactions WHERE user_id = ? " +
                "AND created_at > NOW() - INTERVAL '1 minute'",
                currentUserId
        )).intValue();

        assertEquals(0, count, "No transaction should be created");
    }

    @Then("the subscription end date should extend by {int} days")
    public void verifySubscriptionExtended(int days) {
        Object endDate = DBConnector.queryValue(
                "SELECT end_date FROM subscriptions WHERE id = ?",
                currentSubscriptionId
        );

        LocalDate end = ((java.sql.Date) endDate).toLocalDate();
        LocalDate expectedEnd = LocalDate.now().plusDays(days);

        long daysDiff = ChronoUnit.DAYS.between(expectedEnd, end);

        assertTrue(
                Math.abs(daysDiff) <= 1,
                "Subscription should extend by " + days + " days"
        );
    }

    @Then("a new transaction should be recorded")
    public void verifyNewTransaction() {
        DBAssertions.assertRowExists("transactions", "user_id", currentUserId);
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private String createTestUser(String email) {
        DBConnector.execute(
                "INSERT INTO users (email, phone, name, status) VALUES (?, ?, ?, ?)",
                email, "+14155551000", "Billing Test User", "ACTIVE"
        );

        return DBConnector.queryValue(
                "SELECT id FROM users WHERE email = ?",
                email
        ).toString();
    }
}

