package com.company.qa.unified.config;

import com.company.qa.unified.utils.JsonUtils;
import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Read-only Feature Flag client for automation tests.
 *
 * Priority order:
 * 1. Forced overrides via JVM system properties
 * 2. Cached value
 * 3. Remote feature-flag service
 *
 * Tests MUST NOT mutate flags.
 */
public final class FeatureFlagClient {

    private static final Log log = Log.get(FeatureFlagClient.class);

    private static final EnvironmentConfig ENV = EnvironmentConfig.get();

    private static final Duration CACHE_TTL = Duration.ofMinutes(2);

    private static final Map<String, CachedFlag> CACHE =
            new ConcurrentHashMap<>();

    private FeatureFlagClient() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Check if a feature flag is enabled for a user.
     */
    public static boolean isEnabled(
            String flagKey,
            String userId
    ) {
        Objects.requireNonNull(flagKey, "flagKey cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");

        // 1️⃣ Forced override
        Optional<Boolean> forced =
                getForcedFlag(flagKey);
        if (forced.isPresent()) {
            log.debug("Using forced feature flag {}={}", flagKey, forced.get());
            return forced.get();
        }

        // 2️⃣ Cache
        String cacheKey = flagKey + ":" + userId;
        CachedFlag cached = CACHE.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.enabled;
        }

        // 3️⃣ Remote fetch
        boolean enabled =
                fetchFlagFromService(flagKey, userId);

        CACHE.put(cacheKey,
                new CachedFlag(enabled, Instant.now()));

        return enabled;
    }

    /**
     * Assert a feature flag state explicitly.
     */
    public static void assertFlagState(
            String flagKey,
            String userId,
            boolean expected
    ) {
        boolean actual = isEnabled(flagKey, userId);
        if (actual != expected) {
            fail("Feature flag state mismatch for " + flagKey +
                    " expected=" + expected +
                    " actual=" + actual);
        }
    }

    /* =========================================================
       FORCED OVERRIDES (TEST CONTROL)
       ========================================================= */

    /**
     * Force a feature flag via JVM property:
     *
     * -Dflag.new_checkout=true
     */
    private static Optional<Boolean> getForcedFlag(String flagKey) {
        String value = System.getProperty("flag." + flagKey);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(value));
    }

    /* =========================================================
       REMOTE FEATURE FLAG FETCH
       ========================================================= */

    @SuppressWarnings("unchecked")
    private static boolean fetchFlagFromService(
            String flagKey,
            String userId
    ) {
        if (ENV.isProd()) {
            log.warn("Feature flag read in PROD: {}", flagKey);
        }

        try {
            Response response =
                    given()
                            .baseUri(ENV.getApiBaseUrl())
                            .queryParam("flag", flagKey)
                            .queryParam("userId", userId)
                            .when()
                            .get("/feature-flags/evaluate")
                            .then()
                            .statusCode(200)
                            .extract()
                            .response();

            Map<String, Object> body =
                    JsonUtils.fromJson(response.asString(), Map.class);

            Object enabled = body.get("enabled");
            if (enabled == null) {
                throw new IllegalStateException(
                        "Feature flag response missing 'enabled' field");
            }

            return Boolean.parseBoolean(enabled.toString());

        } catch (Exception e) {
            fail("Failed to fetch feature flag " + flagKey +
                    ": " + e.getMessage());
            return false; // unreachable
        }
    }

    /* =========================================================
       CACHE MODEL
       ========================================================= */

    private static final class CachedFlag {
        private final boolean enabled;
        private final Instant fetchedAt;

        private CachedFlag(boolean enabled, Instant fetchedAt) {
            this.enabled = enabled;
            this.fetchedAt = fetchedAt;
        }

        private boolean isExpired() {
            return fetchedAt.plus(CACHE_TTL)
                    .isBefore(Instant.now());
        }
    }
}
