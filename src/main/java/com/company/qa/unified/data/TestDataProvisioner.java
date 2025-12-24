package com.company.qa.unified.data;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.DBConnector;
import com.company.qa.unified.utils.Log;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * TestDataProvisioner is responsible for:
 *
 * - Seeding data into backend systems (DB / APIs)
 * - Preparing preconditions for tests
 * - Cleaning up test data after execution
 *
 * IMPORTANT:
 * - This class EXECUTES data provisioning
 * - Data CREATION happens in TestDataFactory / domain factories
 *
 * Separation of concerns is intentional.
 */
public final class TestDataProvisioner {

    private static final Log log =
            Log.get(TestDataProvisioner.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private TestDataProvisioner() {
        // utility
    }

    /* =========================================================
       USER PROVISIONING
       ========================================================= */

    /**
     * Ensure a user exists in backend systems.
     * Idempotent by design.
     */
    public static void provisionUser(UserFactory.User user) {

        log.info("Provisioning user: {}", user.userId());

        try {
            DBConnector.execute(
                    "INSERT INTO users (user_id, phone, type, status) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (user_id) DO NOTHING",
                    user.userId(),
                    user.phone(),
                    user.userType().name(),
                    "ACTIVE"
            );

        } catch (Exception e) {
            fail("Failed to provision user: " + e.getMessage());
        }
    }

    /* =========================================================
       SUBSCRIPTION PROVISIONING
       ========================================================= */

    /**
     * Provision a premium subscription for a user.
     */
    public static void provisionSubscription(
            String userId,
            Map<String, Object> subscriptionData
    ) {
        log.info("Provisioning subscription for user={}", userId);

        try {
            DBConnector.execute(
                    "INSERT INTO subscriptions " +
                            "(user_id, plan_id, status, valid_until) " +
                            "VALUES (?, ?, ?, NOW() + INTERVAL '30 days') " +
                            "ON CONFLICT (user_id) DO UPDATE SET status = EXCLUDED.status",
                    userId,
                    subscriptionData.get("planId"),
                    subscriptionData.getOrDefault("status", "ACTIVE")
            );

        } catch (Exception e) {
            fail("Failed to provision subscription: " + e.getMessage());
        }
    }

    /* =========================================================
       PAYMENT PROVISIONING
       ========================================================= */

    /**
     * Provision a payment record (success / failure).
     */
    public static void provisionPayment(
            PaymentDataFactory.PaymentRequest payment
    ) {
        log.info("Provisioning payment: {}", payment.paymentId());

        try {
            DBConnector.execute(
                    "INSERT INTO payments " +
                            "(payment_id, user_id, amount, currency, status) " +
                            "VALUES (?, ?, ?, ?, ?) " +
                            "ON CONFLICT (payment_id) DO NOTHING",
                    payment.paymentId(),
                    payment.userId(),
                    payment.amount(),
                    payment.currency(),
                    payment.simulatedFailure() == null
                            ? "SUCCESS"
                            : "FAILED"
            );

        } catch (Exception e) {
            fail("Failed to provision payment: " + e.getMessage());
        }
    }

    /* =========================================================
       SEARCH / LOOKUP DATA
       ========================================================= */

    /**
     * Seed search index data for deterministic lookup tests.
     */
    public static void provisionSearchIndex(
            SearchDataFactory.SearchRequest searchRequest
    ) {
        log.info("Provisioning search index for query={}",
                searchRequest.query());

        try {
            DBConnector.execute(
                    "INSERT INTO search_index " +
                            "(query, type, country_code, language) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT DO NOTHING",
                    searchRequest.query(),
                    searchRequest.type().name(),
                    searchRequest.countryCode(),
                    searchRequest.language()
            );

        } catch (Exception e) {
            fail("Failed to provision search index: " + e.getMessage());
        }
    }

    /* =========================================================
       CLEANUP
       ========================================================= */

    /**
     * Cleanup all test data for a user.
     * Safe to call multiple times.
     */
    public static void cleanupUserData(String userId) {

        if (ENV.isProd()) {
            fail("❌ Test data cleanup is not allowed in PROD");
        }

        log.info("Cleaning up test data for user={}", userId);

        try {
            DBConnector.execute(
                    "DELETE FROM payments WHERE user_id = ?", userId);
            DBConnector.execute(
                    "DELETE FROM subscriptions WHERE user_id = ?", userId);
            DBConnector.execute(
                    "DELETE FROM users WHERE user_id = ?", userId);

        } catch (Exception e) {
            fail("Failed to cleanup test data: " + e.getMessage());
        }
    }

    /* =========================================================
       COMPOSITE PROVISIONING (E2E)
       ========================================================= */

    /**
     * Provision everything needed for UserUpgradeToPremiumE2E.
     */
    public static void provisionUserUpgradeScenario(
            TestDataFactory.UserUpgradeData data
    ) {
        log.info("Provisioning UserUpgradeToPremium scenario");

        provisionUser(data.user());
        provisionPayment(data.payment());
    }
}
