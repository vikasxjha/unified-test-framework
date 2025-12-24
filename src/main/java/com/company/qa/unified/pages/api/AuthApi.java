package com.company.qa.unified.pages.api;

import com.company.qa.unified.data.UserFactory;
import com.company.qa.unified.drivers.APIClientFactory;
import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * AuthApi encapsulates authentication-related APIs.
 *
 * Responsibilities:
 * - Login
 * - OTP verification
 * - Token refresh
 * - Logout
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS go through AuthApi
 */
public final class AuthApi {

    private static final Log log =
            Log.get(AuthApi.class);

    private AuthApi() {
        // utility
    }

    /* =========================================================
       LOGIN
       ========================================================= */

    /**
     * Initiate login (OTP / passwordless flow).
     */
    public static Response login(UserFactory.User user) {

        log.info("🔐 Initiating login for user={}", user.userId());

        return APIClientFactory.defaultClient()
                .body(Map.of(
                        "phone", user.phone(),
                        "countryCode", user.countryCode()
                ))
                .post("/auth/login")
                .then()
                .extract()
                .response();
    }

    /* =========================================================
       OTP VERIFICATION
       ========================================================= */

    /**
     * Verify OTP and retrieve auth token.
     *
     * NOTE:
     * - OTP is mocked / test-controlled in non-prod
     */
    public static AuthSession verifyOtp(
            UserFactory.User user,
            String otp
    ) {

        log.info("🔑 Verifying OTP for user={}", user.userId());

        Response response =
                APIClientFactory.defaultClient()
                        .body(Map.of(
                                "phone", user.phone(),
                                "otp", otp
                        ))
                        .post("/auth/verify-otp")
                        .then()
                        .extract()
                        .response();

        if (response.statusCode() != 200) {
            fail("OTP verification failed: "
                    + response.asString());
        }

        String accessToken =
                response.path("accessToken");

        String refreshToken =
                response.path("refreshToken");

        return new AuthSession(accessToken, refreshToken);
    }

    /* =========================================================
       TOKEN REFRESH
       ========================================================= */

    public static AuthSession refreshToken(String refreshToken) {

        log.info("🔄 Refreshing auth token");

        Response response =
                APIClientFactory.defaultClient()
                        .body(Map.of(
                                "refreshToken", refreshToken
                        ))
                        .post("/auth/refresh")
                        .then()
                        .extract()
                        .response();

        if (response.statusCode() != 200) {
            fail("Token refresh failed: "
                    + response.asString());
        }

        return new AuthSession(
                response.path("accessToken"),
                response.path("refreshToken")
        );
    }

    /* =========================================================
       LOGOUT
       ========================================================= */

    public static void logout(String accessToken) {

        log.info("🚪 Logging out");

        APIClientFactory.customClient(
                        Map.of("Authorization",
                                "Bearer " + accessToken)
                )
                .post("/auth/logout")
                .then()
                .statusCode(204);
    }

    /* =========================================================
       SESSION MODEL
       ========================================================= */

    /**
     * Immutable auth session model.
     */
    public static final class AuthSession {

        private final String accessToken;
        private final String refreshToken;

        public AuthSession(
                String accessToken,
                String refreshToken
        ) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String accessToken() {
            return accessToken;
        }

        public String refreshToken() {
            return refreshToken;
        }

        /**
         * Create an authenticated API client
         * for downstream calls.
         */
        public io.restassured.specification.RequestSpecification
        authenticatedClient() {

            return APIClientFactory.customClient(
                    Map.of(
                            "Authorization",
                            "Bearer " + accessToken
                    )
            );
        }
    }
}
