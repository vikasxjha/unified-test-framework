package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * SubscriptionApi
 *
 * Encapsulates subscription lifecycle APIs.
 *
 * Covers:
 * - Fetch current subscription
 * - Upgrade / downgrade plans
 * - Pause / resume subscription
 * - Auto-renew control
 * - Trial management
 * - Admin subscription overrides
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use SubscriptionApi
 */
public class SubscriptionApi extends BaseApiClient {

    private static final Log log =
            Log.get(SubscriptionApi.class);

    /* =========================================================
       FETCH SUBSCRIPTION
       ========================================================= */

    /**
     * Fetch current subscription for user.
     */
    public Response getCurrentSubscription(String accessToken) {

        log.info("📄 Fetching current subscription");

        Response response =
                get(
                        authenticated(accessToken),
                        "/subscription/current"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PLAN CHANGE
       ========================================================= */

    /**
     * Upgrade subscription plan.
     */
    public Response upgrade(
            String accessToken,
            String newPlanId
    ) {

        log.info("⬆️ Upgrading subscription to plan={}", newPlanId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/upgrade",
                        Map.of("planId", newPlanId)
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Downgrade subscription plan.
     */
    public Response downgrade(
            String accessToken,
            String newPlanId
    ) {

        log.info("⬇️ Downgrading subscription to plan={}", newPlanId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/downgrade",
                        Map.of("planId", newPlanId)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PAUSE / RESUME
       ========================================================= */

    /**
     * Pause an active subscription.
     */
    public Response pause(String accessToken) {

        log.info("⏸ Pausing subscription");

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/pause",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Resume a paused subscription.
     */
    public Response resume(String accessToken) {

        log.info("▶️ Resuming subscription");

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/resume",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       AUTO-RENEW
       ========================================================= */

    /**
     * Enable or disable auto-renew.
     */
    public Response setAutoRenew(
            String accessToken,
            boolean enabled
    ) {

        log.info("🔁 Setting auto-renew={}", enabled);

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/auto-renew",
                        Map.of("enabled", enabled)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       TRIAL MANAGEMENT
       ========================================================= */

    /**
     * Start a trial subscription.
     */
    public Response startTrial(
            String accessToken,
            String planId
    ) {

        log.info("🧪 Starting trial for plan={}", planId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/trial/start",
                        Map.of("planId", planId)
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * End trial early.
     */
    public Response endTrial(String accessToken) {

        log.info("🛑 Ending trial");

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/trial/end",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       CANCELLATION
       ========================================================= */

    /**
     * Cancel subscription.
     */
    public Response cancel(String accessToken) {

        log.info("❌ Cancelling subscription");

        Response response =
                post(
                        authenticated(accessToken),
                        "/subscription/cancel",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN / OVERRIDES
       ========================================================= */

    /**
     * Force activate subscription (admin-only).
     */
    public Response adminActivate(
            String userId,
            String planId
    ) {

        log.info("🛠 Admin activating subscription user={} plan={}",
                userId, planId);

        Response response =
                post(
                        admin(),
                        "/admin/subscription/activate",
                        Map.of(
                                "userId", userId,
                                "planId", planId
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Force cancel subscription (admin-only).
     */
    public Response adminCancel(String userId) {

        log.info("🛠 Admin cancelling subscription user={}",
                userId);

        Response response =
                post(
                        admin(),
                        "/admin/subscription/cancel",
                        Map.of("userId", userId)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertActive(Response response) {

        String status = response.path("status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            fail("Expected ACTIVE subscription but got " + status);
        }
    }

    public static void assertCancelled(Response response) {

        String status = response.path("status");
        if (!"CANCELLED".equalsIgnoreCase(status)) {
            fail("Expected CANCELLED subscription but got " + status);
        }
    }

    public static void assertPlan(Response response, String planId) {

        String actualPlan = response.path("planId");
        if (!planId.equals(actualPlan)) {
            fail("Expected plan=" + planId +
                    " but got=" + actualPlan);
        }
    }

    public static void assertTrial(Response response) {

        Boolean trial = response.path("trial");
        if (!Boolean.TRUE.equals(trial)) {
            fail("Expected TRIAL subscription but was not");
        }
    }
}
