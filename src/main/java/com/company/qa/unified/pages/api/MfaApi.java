package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * MfaApi
 *
 * Encapsulates Multi-Factor Authentication APIs.
 *
 * Supports:
 * - MFA challenge initiation
 * - OTP / TOTP verification
 * - Push approval verification
 * - MFA status checks
 * - Admin reset / unenroll
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use MfaApi
 */
public class MfaApi extends BaseApiClient {

    private static final Log log =
            Log.get(MfaApi.class);

    /* =========================================================
       MFA CHALLENGE
       ========================================================= */

    /**
     * Initiate an MFA challenge.
     *
     * @param accessToken user access token
     * @param type OTP / TOTP / PUSH
     */
    public Response initiateChallenge(
            String accessToken,
            MfaType type
    ) {

        log.info("🔐 Initiating MFA challenge type={}", type);

        Response response =
                post(
                        authenticated(accessToken),
                        "/mfa/challenge",
                        Map.of("type", type.name())
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       OTP / TOTP VERIFICATION
       ========================================================= */

    /**
     * Verify OTP / TOTP code.
     */
    public Response verifyCode(
            String accessToken,
            String challengeId,
            String code
    ) {

        log.info("🔑 Verifying MFA code for challenge={}", challengeId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/mfa/verify",
                        Map.of(
                                "challengeId", challengeId,
                                "code", code
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PUSH APPROVAL
       ========================================================= */

    /**
     * Poll push-based MFA approval.
     */
    public Response pollPushApproval(
            String accessToken,
            String challengeId
    ) {

        log.info("📲 Polling push MFA approval challenge={}",
                challengeId);

        Response response =
                get(
                        authenticated(accessToken),
                        "/mfa/push/" + challengeId + "/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       MFA STATUS
       ========================================================= */

    /**
     * Fetch MFA enrollment & status for current user.
     */
    public Response getMfaStatus(String accessToken) {

        log.info("📡 Fetching MFA status");

        Response response =
                get(
                        authenticated(accessToken),
                        "/mfa/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN OPERATIONS
       ========================================================= */

    /**
     * Reset MFA for a user (admin-only).
     */
    public Response resetMfaForUser(String userId) {

        log.info("🛠 Resetting MFA for user={}", userId);

        Response response =
                post(
                        admin(),
                        "/admin/mfa/reset",
                        Map.of("userId", userId)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertMfaRequired(Response response) {
        Boolean required = response.path("mfaRequired");
        if (!Boolean.TRUE.equals(required)) {
            fail("MFA expected to be REQUIRED but was not");
        }
    }

    public static void assertMfaVerified(Response response) {
        String status = response.path("status");
        if (!"VERIFIED".equalsIgnoreCase(status)) {
            fail("MFA verification failed. Status=" + status);
        }
    }

    public static void assertMfaNotEnrolled(Response response) {
        Boolean enrolled = response.path("enrolled");
        if (Boolean.TRUE.equals(enrolled)) {
            fail("MFA expected to be NOT enrolled but was enrolled");
        }
    }

    /* =========================================================
       ENUMS
       ========================================================= */

    public enum MfaType {
        OTP,
        TOTP,
        PUSH
    }
}
