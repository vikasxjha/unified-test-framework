package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * UserApi
 *
 * Encapsulates user profile & account related APIs.
 *
 * Covers:
 * - User profile fetch/update
 * - Account settings
 * - Device management
 * - Account state (block / deactivate)
 * - Admin overrides
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use UserApi
 */
public class UserApi extends BaseApiClient {

    private static final Log log =
            Log.get(UserApi.class);

    /* =========================================================
       PROFILE
       ========================================================= */

    /**
     * Fetch logged-in user's profile.
     */
    public Response getProfile(String accessToken) {

        log.info("👤 Fetching user profile");

        Response response =
                get(
                        authenticated(accessToken),
                        "/user/profile"
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Update user profile.
     */
    public Response updateProfile(
            String accessToken,
            Map<String, Object> updates
    ) {

        log.info("✏️ Updating user profile fields={}",
                updates.keySet());

        Response response =
                put(
                        authenticated(accessToken),
                        "/user/profile",
                        updates
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ACCOUNT SETTINGS
       ========================================================= */

    /**
     * Fetch user account settings.
     */
    public Response getSettings(String accessToken) {

        log.info("⚙️ Fetching user settings");

        Response response =
                get(
                        authenticated(accessToken),
                        "/user/settings"
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Update user account settings.
     */
    public Response updateSettings(
            String accessToken,
            Map<String, Object> settings
    ) {

        log.info("⚙️ Updating user settings={}",
                settings.keySet());

        Response response =
                put(
                        authenticated(accessToken),
                        "/user/settings",
                        settings
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       DEVICE MANAGEMENT
       ========================================================= */

    /**
     * Fetch devices associated with the account.
     */
    public Response getDevices(String accessToken) {

        log.info("📱 Fetching user devices");

        Response response =
                get(
                        authenticated(accessToken),
                        "/user/devices"
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Revoke a specific device session.
     */
    public Response revokeDevice(
            String accessToken,
            String deviceId
    ) {

        log.info("🔌 Revoking device deviceId={}", deviceId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/user/devices/revoke",
                        Map.of("deviceId", deviceId)
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Revoke all devices except current.
     */
    public Response revokeAllOtherDevices(String accessToken) {

        log.info("🔌 Revoking all other devices");

        Response response =
                post(
                        authenticated(accessToken),
                        "/user/devices/revoke-all",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ACCOUNT STATE
       ========================================================= */

    /**
     * Deactivate user account.
     */
    public Response deactivateAccount(
            String accessToken,
            String reason
    ) {

        log.info("⛔ Deactivating account reason={}", reason);

        Response response =
                post(
                        authenticated(accessToken),
                        "/user/deactivate",
                        Map.of("reason", reason)
                );

        assertStatus(response, 202);
        return response;
    }

    /**
     * Reactivate user account (admin-only).
     */
    public Response adminReactivateAccount(String userId) {

        log.info("🛠 Reactivating account user={}", userId);

        Response response =
                post(
                        admin(),
                        "/admin/user/reactivate",
                        Map.of("userId", userId)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN / INTERNAL
       ========================================================= */

    /**
     * Fetch raw user data (admin/debug).
     */
    public Response adminGetUser(String userId) {

        log.info("🛠 Fetching admin user profile user={}", userId);

        Response response =
                get(
                        admin(),
                        "/admin/user/" + userId
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Block user (admin-only).
     */
    public Response adminBlockUser(
            String userId,
            String reason
    ) {

        log.info("🚫 Blocking user={} reason={}",
                userId, reason);

        Response response =
                post(
                        admin(),
                        "/admin/user/block",
                        Map.of(
                                "userId", userId,
                                "reason", reason
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Unblock user (admin-only).
     */
    public Response adminUnblockUser(String userId) {

        log.info("✅ Unblocking user={}", userId);

        Response response =
                post(
                        admin(),
                        "/admin/user/unblock",
                        Map.of("userId", userId)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertProfileName(
            Response response,
            String expectedName
    ) {

        String actual = response.path("name");
        if (!expectedName.equals(actual)) {
            fail("Expected name=" + expectedName +
                    " but got=" + actual);
        }
    }

    public static void assertAccountActive(Response response) {

        String status = response.path("status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            fail("Expected ACTIVE account but got " + status);
        }
    }

    public static void assertAccountDeactivated(Response response) {

        String status = response.path("status");
        if (!"DEACTIVATED".equalsIgnoreCase(status)) {
            fail("Expected DEACTIVATED account but got " + status);
        }
    }
}
