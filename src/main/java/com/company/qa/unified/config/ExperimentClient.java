package com.company.qa.unified.config;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.JsonUtils;
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
 * Read-only client for fetching experiment / A-B test variants.
 *
 * Responsibilities:
 * - Resolve experiment variants for a user
 * - Allow deterministic overrides for tests
 * - Cache results to avoid repeated calls
 *
 * This client MUST NOT:
 * - mutate experiments
 * - enroll users
 * - write state
 */
public final class ExperimentClient {

    private static final Log log = Log.get(ExperimentClient.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private static final Map<String, CachedVariant> CACHE =
            new ConcurrentHashMap<>();

    private static final EnvironmentConfig ENV = EnvironmentConfig.get();

    private ExperimentClient() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Get experiment variant for a user.
     *
     * @param experimentKey logical experiment name
     * @param userId stable user identifier
     */
    public static String getVariant(
            String experimentKey,
            String userId
    ) {
        Objects.requireNonNull(experimentKey, "experimentKey cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");

        // 1️⃣ Forced override (highest priority)
        Optional<String> forced =
                getForcedVariant(experimentKey);
        if (forced.isPresent()) {
            log.debug("Using forced variant for experiment={} variant={}",
                    experimentKey, forced.get());
            return forced.get();
        }

        // 2️⃣ Cached value
        String cacheKey = experimentKey + ":" + userId;
        CachedVariant cached = CACHE.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.variant;
        }

        // 3️⃣ Fetch from experiment service
        String variant = fetchVariantFromService(experimentKey, userId);

        CACHE.put(cacheKey,
                new CachedVariant(variant, Instant.now()));

        return variant;
    }

    /**
     * Check if a user is in a specific variant.
     */
    public static boolean isVariant(
            String experimentKey,
            String userId,
            String expectedVariant
    ) {
        return expectedVariant.equals(
                getVariant(experimentKey, userId)
        );
    }

    /* =========================================================
       FORCED VARIANTS (TESTING)
       ========================================================= */

    /**
     * Force experiment variant via system property:
     *
     * -Dexperiment.checkout_flow=B
     */
    private static Optional<String> getForcedVariant(String experimentKey) {
        String value = System.getProperty("experiment." + experimentKey);
        return (value == null || value.isBlank())
                ? Optional.empty()
                : Optional.of(value);
    }

    /* =========================================================
       REMOTE FETCH
       ========================================================= */

    @SuppressWarnings("unchecked")
    private static String fetchVariantFromService(
            String experimentKey,
            String userId
    ) {
        if (ENV.isProd()) {
            log.warn("Experiment lookup in PROD for key={}", experimentKey);
        }

        try {
            Response response =
                    given()
                            .baseUri(ENV.getApiBaseUrl())
                            .queryParam("experiment", experimentKey)
                            .queryParam("userId", userId)
                            .when()
                            .get("/experiments/variant")
                            .then()
                            .statusCode(200)
                            .extract()
                            .response();

            Map<String, Object> body =
                    JsonUtils.fromJson(
                            response.asString(), Map.class);

            Object variant = body.get("variant");
            if (variant == null) {
                throw new IllegalStateException(
                        "Variant missing in experiment response");
            }

            return variant.toString();

        } catch (Exception e) {
            fail("Failed to fetch experiment variant for " +
                    experimentKey + ": " + e.getMessage());
            return "CONTROL"; // unreachable
        }
    }

    /* =========================================================
       CACHE MODEL
       ========================================================= */

    private static final class CachedVariant {
        private final String variant;
        private final Instant fetchedAt;

        private CachedVariant(String variant, Instant fetchedAt) {
            this.variant = variant;
            this.fetchedAt = fetchedAt;
        }

        private boolean isExpired() {
            return fetchedAt.plus(CACHE_TTL)
                    .isBefore(Instant.now());
        }
    }
}
