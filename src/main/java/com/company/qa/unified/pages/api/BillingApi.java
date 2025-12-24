package com.company.qa.unified.pages.api;

import com.company.qa.unified.data.PaymentDataFactory;
import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * BillingApi encapsulates all billing & payment related APIs.
 *
 * Covers:
 * - Subscription purchase
 * - One-time charges
 * - Payment status
 * - Refunds
 * - Pricing preview
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS go through BillingApi
 */
public class BillingApi extends BaseApiClient {

    private static final Log log =
            Log.get(BillingApi.class);

    /* =========================================================
       SUBSCRIPTIONS
       ========================================================= */

    /**
     * Subscribe user to a plan.
     */
    public Response subscribe(
            String accessToken,
            PaymentDataFactory.PaymentRequest payment
    ) {

        log.info("💳 Subscribing user={} plan={}",
                payment.userId(), payment.planId());

        Response response =
                post(
                        authenticated(accessToken),
                        "/billing/subscribe",
                        payment.toApiPayload()
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Cancel active subscription.
     */
    public Response cancelSubscription(String accessToken) {

        log.info("❌ Cancelling subscription");

        Response response =
                post(
                        authenticated(accessToken),
                        "/billing/subscription/cancel",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ONE-TIME PAYMENTS
       ========================================================= */

    public Response charge(
            String accessToken,
            PaymentDataFactory.PaymentRequest payment
    ) {

        log.info("💰 Charging paymentId={}",
                payment.paymentId());

        Response response =
                post(
                        authenticated(accessToken),
                        "/billing/charge",
                        payment.toApiPayload()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PAYMENT STATUS
       ========================================================= */

    public Response getPaymentStatus(
            String accessToken,
            String paymentId
    ) {

        log.info("🔎 Fetching payment status paymentId={}",
                paymentId);

        Response response =
                get(
                        authenticated(accessToken),
                        "/billing/payment/" + paymentId
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       REFUNDS
       ========================================================= */

    public Response refund(
            String accessToken,
            PaymentDataFactory.RefundRequest refund
    ) {

        log.info("↩️ Initiating refund paymentId={}",
                refund.toApiPayload().get("paymentId"));

        Response response =
                post(
                        authenticated(accessToken),
                        "/billing/refund",
                        refund.toApiPayload()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PRICING / PREVIEW
       ========================================================= */

    /**
     * Preview price before purchase (tax, discounts, etc).
     */
    public Response preview(
            String accessToken,
            String planId
    ) {

        log.info("🧾 Pricing preview plan={}", planId);

        Response response =
                get(
                        authenticated(accessToken),
                        "/billing/preview?planId=" + planId
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN / INTERNAL (OPTIONAL)
       ========================================================= */

    /**
     * Fetch all subscriptions (admin-only).
     */
    public Response listAllSubscriptions() {

        log.info("🛠 Fetching all subscriptions (admin)");

        Response response =
                get(
                        admin(),
                        "/admin/billing/subscriptions"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertPaymentSuccess(Response response) {

        String status = response.path("status");
        if (!"SUCCESS".equalsIgnoreCase(status)) {
            fail("Payment expected SUCCESS but was: " + status);
        }
    }

    public static void assertSubscriptionActive(Response response) {

        String status = response.path("subscription.status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            fail("Subscription not ACTIVE: " + status);
        }
    }
}
