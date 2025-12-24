package com.company.qa.unified.drivers;

import com.company.qa.unified.config.CredentialsProvider;
import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.utils.Log;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central factory for API clients.
 *
 * Responsibilities:
 * - Configure base URI per environment
 * - Inject auth headers / tokens
 * - Apply timeouts & logging
 * - Provide reusable RequestSpecification instances
 *
 * RULE:
 * ❌ Tests must NOT configure RestAssured directly
 * ✅ Tests must ALWAYS use APIClientFactory
 */
public final class APIClientFactory {

    private static final Log log =
            Log.get(APIClientFactory.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    /**
     * Cache per logical client (auth/user/admin/etc.)
     */
    private static final Map<String, RequestSpecification> CACHE =
            new ConcurrentHashMap<>();

    private APIClientFactory() {
        // utility
    }

    /* =========================================================
       PUBLIC ENTRY POINTS
       ========================================================= */

    /**
     * Default API client (no auth).
     */
    public static RequestSpecification defaultClient() {
        return getOrCreate("default", AuthType.NONE);
    }

    /**
     * Authenticated client using API token.
     */
    public static RequestSpecification authenticatedClient() {
        return getOrCreate("auth", AuthType.TOKEN);
    }

    /**
     * Admin API client.
     */
    public static RequestSpecification adminClient() {
        return getOrCreate("admin", AuthType.ADMIN_TOKEN);
    }

    /**
     * Client with custom headers (one-off use).
     */
    public static RequestSpecification customClient(
            Map<String, String> headers
    ) {
        RequestSpecBuilder builder =
                baseSpecBuilder(AuthType.NONE);

        headers.forEach(builder::addHeader);
        return builder.build();
    }

    /* =========================================================
       CORE FACTORY LOGIC
       ========================================================= */

    private static RequestSpecification getOrCreate(
            String key,
            AuthType authType
    ) {
        return CACHE.computeIfAbsent(key, k -> {
            log.info("Creating API client: {}", key);
            return baseSpecBuilder(authType).build();
        });
    }

    private static RequestSpecBuilder baseSpecBuilder(
            AuthType authType
    ) {
        RequestSpecBuilder builder =
                new RequestSpecBuilder()
                        .setBaseUri(ENV.getApiBaseUrl())
                        .setContentType(ContentType.JSON)
                        .addHeader("X-Request-Id", UUID.randomUUID().toString())
                        .addHeader("User-Agent", "unified-test-framework");

        applyAuth(builder, authType);
        applyTimeouts();
        applyLogging();

        return builder;
    }

    /* =========================================================
       AUTH HANDLING
       ========================================================= */

    private static void applyAuth(
            RequestSpecBuilder builder,
            AuthType authType
    ) {
        switch (authType) {
            case TOKEN -> {
                String token =
                        CredentialsProvider.getSecret("api.token");
                builder.addHeader(
                        "Authorization", "Bearer " + token);
            }
            case ADMIN_TOKEN -> {
                String token =
                        CredentialsProvider.getSecret("admin.api.token");
                builder.addHeader(
                        "Authorization", "Bearer " + token);
            }
            case NONE -> {
                // no-op
            }
        }
    }

    /* =========================================================
       REST ASSURED GLOBAL CONFIG
       ========================================================= */

    private static void applyTimeouts() {
        RestAssured.config =
                RestAssured.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam(
                                        "http.connection.timeout",
                                        (int) Duration.ofSeconds(10).toMillis()
                                )
                                .setParam(
                                        "http.socket.timeout",
                                        (int) Duration.ofSeconds(30).toMillis()
                                )
                );
    }

    private static void applyLogging() {
        if (!RuntimeConfig.environment().isProd()) {
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(
                    LogDetail.ALL
            );
        }
    }

    /* =========================================================
       AUTH TYPES
       ========================================================= */

    private enum AuthType {
        NONE,
        TOKEN,
        ADMIN_TOKEN
    }
}
