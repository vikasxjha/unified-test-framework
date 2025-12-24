package com.company.qa.unified.data;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.utils.Log;

import java.util.List;
import java.util.Map;

/**
 * Central facade for ALL test data creation.
 *
 * Why this exists:
 * - StepDefs / Tests should NOT directly call multiple factories
 * - Provides a single, discoverable entry point for test data
 * - Enforces consistency across Web / API / Mobile / DB / Events
 *
 * Rule:
 * ❌ Tests should NOT instantiate data manually
 * ✅ Tests should ALWAYS go through TestDataFactory
 */
public final class TestDataFactory {

    private static final Log log =
            Log.get(TestDataFactory.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private TestDataFactory() {
        // utility
    }

    /* =========================================================
       USER DATA
       ========================================================= */

    public static UserFactory.User freeUser() {
        log.debug("Creating free user test data");
        return UserFactory.freeUser();
    }

    public static UserFactory.User premiumUser() {
        log.debug("Creating premium user test data");
        return UserFactory.premiumUser();
    }

    public static UserFactory.User adminUser() {
        log.debug("Creating admin user test data");
        return UserFactory.adminUser();
    }

    /* =========================================================
       PAYMENT DATA
       ========================================================= */

    public static PaymentDataFactory.PaymentRequest
    premiumSubscriptionPayment() {
        log.debug("Creating premium subscription payment data");
        return PaymentDataFactory.premiumSubscription();
    }

    public static PaymentDataFactory.PaymentRequest
    yearlySubscriptionPayment() {
        return PaymentDataFactory.yearlySubscription();
    }

    public static PaymentDataFactory.PaymentRequest
    failingPaymentInsufficientBalance() {
        return PaymentDataFactory.insufficientBalance();
    }

    public static PaymentDataFactory.RefundRequest
    fullRefund(String paymentId) {
        return PaymentDataFactory.fullRefund(paymentId);
    }

    /* =========================================================
       SEARCH DATA
       ========================================================= */

    public static SearchDataFactory.SearchRequest
    validPhoneSearch() {
        return SearchDataFactory.validPhoneLookup();
    }

    public static SearchDataFactory.SearchRequest
    spamPhoneSearch() {
        return SearchDataFactory.spamPhoneLookup();
    }

    public static SearchDataFactory.SearchRequest
    businessSearch() {
        return SearchDataFactory.businessSearch();
    }

    public static List<SearchDataFactory.SearchRequest>
    bulkPhoneSearch(int count) {
        return SearchDataFactory.bulkSearchRequests(count);
    }

    /* =========================================================
       GOLDEN DATASETS
       ========================================================= */

    public static Map<String, Object>
    goldenLookupSuccess() {
        return GoldenDatasetRegistry
                .getPayload("lookup.basic.success");
    }

    public static Map<String, Object>
    goldenPremiumSubscription() {
        return GoldenDatasetRegistry
                .getPayload("subscription.premium.active");
    }

    public static Map<String, Object>
    goldenNotificationSent() {
        return GoldenDatasetRegistry
                .getPayload("notification.push.sent");
    }

    /* =========================================================
       COMPOSITE / E2E DATA
       ========================================================= */

    /**
     * Data bundle used in UserUpgradeToPremiumE2ETest.
     */
    public static UserUpgradeData
    userUpgradeToPremiumBundle() {

        UserFactory.User user = freeUser();

        PaymentDataFactory.PaymentRequest payment =
                PaymentDataFactory.premiumSubscription();

        Map<String, Object> goldenSubscription =
                GoldenDatasetRegistry
                        .getPayload("subscription.premium.active");

        return new UserUpgradeData(
                user,
                payment,
                goldenSubscription
        );
    }

    /* =========================================================
       DATA BUNDLES (E2E MODELS)
       ========================================================= */

    /**
     * Immutable bundle for complex E2E scenarios.
     */
    public static final class UserUpgradeData {

        private final UserFactory.User user;
        private final PaymentDataFactory.PaymentRequest payment;
        private final Map<String, Object> expectedSubscription;

        private UserUpgradeData(
                UserFactory.User user,
                PaymentDataFactory.PaymentRequest payment,
                Map<String, Object> expectedSubscription
        ) {
            this.user = user;
            this.payment = payment;
            this.expectedSubscription = expectedSubscription;
        }

        public UserFactory.User user() {
            return user;
        }

        public PaymentDataFactory.PaymentRequest payment() {
            return payment;
        }

        public Map<String, Object> expectedSubscription() {
            return expectedSubscription;
        }
    }

    /* =========================================================
       DEBUG / TRACEABILITY
       ========================================================= */

    /**
     * Useful for logging test data context in reports.
     */
    public static void logEnvironmentSnapshot() {
        log.info("""
                🧪 Test Data Factory Snapshot
                -----------------------------
                Environment : {}
                Web URL     : {}
                API URL     : {}
                """,
                ENV.getEnvironmentName(),
                ENV.getWebBaseUrl(),
                ENV.getApiBaseUrl()
        );
    }
}
