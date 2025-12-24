package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * FeatureFlagApi
 *
 * Encapsulates admin-facing Feature Flag APIs.
 *
 * Capabilities:
 * - Enable / disable flags
 * - Percentage rollouts
 * - User targeting
 * - Flag state verification
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use FeatureFlagApi
 */
public class FeatureFlagApi extends BaseApiClient {

    private static final Log log =
            Log.get(FeatureFlagApi.class);

    /* =========================================================
       FLAG STATE
       ========================================================= */

    /**
     * Enable a feature flag globally.
     */
    public Response enableFlag(String flagKey) {

        log.info("🚩 Enabling feature flag={}", flagKey);

        Response response =
                post(
                        admin(),
                        "/admin/feature-flags/enable",
                        Map.of("flagKey", flagKey)
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Disable a feature flag globally.
     */
    public Response disableFlag(String flagKey) {

        log.info("🏳️ Disabling feature flag={}", flagKey);

        Response response =
                post(
                        admin(),
                        "/admin/feature-flags/disable",
                        Map.of("flagKey", flagKey)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ROLLOUT CONFIGURATION
       ========================================================= */

    /**
     * Set percentage rollout for a flag.
     *
     * @param percentage 0–100
     */
    public Response setPercentageRollout(
            String flagKey,
            int percentage
    ) {

        if (percentage < 0 || percentage > 100) {
            fail("Rollout percentage must be between 0 and 100");
        }

        log.info("📊 Setting rollout={} for flag={}",
                percentage, flagKey);

        Response response =
                post(
                        admin(),
                        "/admin/feature-flags/rollout",
                        Map.of(
                                "flagKey", flagKey,
                                "percentage", percentage
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       USER TARGETING
       ========================================================= */

    /**
     * Enable flag for specific users.
     */
    public Response enableForUsers(
            String flagKey,
            String... userIds
    ) {

        log.info("🎯 Enabling flag={} for users={}",
                flagKey, String.join(",", userIds));

        Response response =
                post(
                        admin(),
                        "/admin/feature-flags/target/users",
                        Map.of(
                                "flagKey", flagKey,
                                "userIds", userIds
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Disable flag for specific users.
     */
    public Response disableForUsers(
            String flagKey,
            String... userIds
    ) {

        log.info("🎯 Disabling flag={} for users={}",
                flagKey, String.join(",", userIds));

        Response response =
                post(
                        admin(),
                        "/admin/feature-flags/untarget/users",
                        Map.of(
                                "flagKey", flagKey,
                                "userIds", userIds
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       FLAG VERIFICATION
       ========================================================= */

    /**
     * Fetch flag state for a specific user.
     */
    public Response getFlagStateForUser(
            String accessToken,
            String flagKey
    ) {

        log.info("🔎 Fetching flag={} for user",
                flagKey);

        Response response =
                get(
                        authenticated(accessToken),
                        "/feature-flags/" + flagKey
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Assert flag is enabled for user.
     */
    public static void assertFlagEnabled(Response response) {
        Boolean enabled = response.path("enabled");
        if (!Boolean.TRUE.equals(enabled)) {
            fail("Feature flag expected ENABLED but was DISABLED");
        }
    }

    /**
     * Assert flag is disabled for user.
     */
    public static void assertFlagDisabled(Response response) {
        Boolean enabled = response.path("enabled");
        if (!Boolean.FALSE.equals(enabled)) {
            fail("Feature flag expected DISABLED but was ENABLED");
        }
    }
}
