package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * PrivacyApi
 *
 * Encapsulates privacy & compliance APIs.
 *
 * Covers:
 * - Data export (GDPR / DSAR)
 * - Data deletion (RTBF)
 * - Consent management
 * - Request status polling
 * - Admin audit & override
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use PrivacyApi
 */
public class PrivacyApi extends BaseApiClient {

    private static final Log log =
            Log.get(PrivacyApi.class);

    /* =========================================================
       DATA EXPORT (DSAR)
       ========================================================= */

    /**
     * Request export of all user data.
     */
    public Response requestDataExport(String accessToken) {

        log.info("📦 Requesting data export");

        Response response =
                post(
                        authenticated(accessToken),
                        "/privacy/export",
                        Map.of()
                );

        assertStatus(response, 202);
        return response;
    }

    /**
     * Fetch export request status.
     */
    public Response getExportStatus(
            String accessToken,
            String requestId
    ) {

        log.info("📡 Fetching export status requestId={}",
                requestId);

        Response response =
                get(
                        authenticated(accessToken),
                        "/privacy/export/" + requestId + "/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       DATA DELETION (RTBF)
       ========================================================= */

    /**
     * Request deletion of user data.
     */
    public Response requestDataDeletion(
            String accessToken,
            String reason
    ) {

        log.info("🗑 Requesting data deletion reason={}",
                reason);

        Response response =
                post(
                        authenticated(accessToken),
                        "/privacy/delete",
                        Map.of("reason", reason)
                );

        assertStatus(response, 202);
        return response;
    }

    /**
     * Fetch deletion request status.
     */
    public Response getDeletionStatus(
            String accessToken,
            String requestId
    ) {

        log.info("📡 Fetching deletion status requestId={}",
                requestId);

        Response response =
                get(
                        authenticated(accessToken),
                        "/privacy/delete/" + requestId + "/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       CONSENT MANAGEMENT
       ========================================================= */

    /**
     * Fetch current consent state.
     */
    public Response getConsent(String accessToken) {

        log.info("🛡 Fetching consent state");

        Response response =
                get(
                        authenticated(accessToken),
                        "/privacy/consent"
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Update consent preferences.
     */
    public Response updateConsent(
            String accessToken,
            Map<String, Boolean> consentFlags
    ) {

        log.info("🛡 Updating consent flags={}",
                consentFlags.keySet());

        Response response =
                put(
                        authenticated(accessToken),
                        "/privacy/consent",
                        consentFlags
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       WAIT / POLLING HELPERS
       ========================================================= */

    /**
     * Wait until a privacy request completes.
     */
    public PrivacyRequestResult waitForCompletion(
            String accessToken,
            String requestId,
            RequestType type,
            Duration timeout
    ) {

        Instant deadline = Instant.now().plus(timeout);

        log.info("⏳ Waiting for privacy request={} type={}",
                requestId, type);

        while (Instant.now().isBefore(deadline)) {

            Response response =
                    type == RequestType.EXPORT
                            ? getExportStatus(accessToken, requestId)
                            : getDeletionStatus(accessToken, requestId);

            String status = response.path("status");

            if (isTerminal(status)) {

                log.info("✅ Privacy request={} completed status={}",
                        requestId, status);

                return new PrivacyRequestResult(
                        requestId,
                        status,
                        response.asString()
                );
            }

            sleep(3000);
        }

        fail("❌ Privacy request did not complete in time: "
                + requestId);
        return null; // unreachable
    }

    private boolean isTerminal(String status) {
        return switch (status) {
            case "COMPLETED",
                 "FAILED",
                 "REJECTED" -> true;
            default -> false;
        };
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    /* =========================================================
       ADMIN / AUDIT
       ========================================================= */

    /**
     * Fetch privacy request audit logs (admin-only).
     */
    public Response getAuditLogs(String requestId) {

        log.info("🛠 Fetching privacy audit logs requestId={}",
                requestId);

        Response response =
                get(
                        admin(),
                        "/admin/privacy/audit/" + requestId
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertCompleted(Response response) {
        String status = response.path("status");
        if (!"COMPLETED".equalsIgnoreCase(status)) {
            fail("Expected COMPLETED but got " + status);
        }
    }

    public static void assertRejected(Response response) {
        String status = response.path("status");
        if (!"REJECTED".equalsIgnoreCase(status)) {
            fail("Expected REJECTED but got " + status);
        }
    }

    /* =========================================================
       MODELS
       ========================================================= */

    public enum RequestType {
        EXPORT,
        DELETE
    }

    /**
     * Immutable privacy request result.
     */
    public static final class PrivacyRequestResult {

        private final String requestId;
        private final String status;
        private final String rawResponse;

        public PrivacyRequestResult(
                String requestId,
                String status,
                String rawResponse
        ) {
            this.requestId = requestId;
            this.status = status;
            this.rawResponse = rawResponse;
        }

        public String requestId() {
            return requestId;
        }

        public String status() {
            return status;
        }

        public boolean isCompleted() {
            return "COMPLETED".equalsIgnoreCase(status);
        }

        public boolean isFailed() {
            return "FAILED".equalsIgnoreCase(status);
        }

        public String rawResponse() {
            return rawResponse;
        }
    }
}
