package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * RecommendationApi
 *
 * Encapsulates recommendation & personalization APIs.
 *
 * Covers:
 * - Personalized recommendations
 * - Contextual recommendations
 * - Experiment / variant-based results
 * - Fallback handling
 * - Admin debug endpoints
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use RecommendationApi
 */
public class RecommendationApi extends BaseApiClient {

    private static final Log log =
            Log.get(RecommendationApi.class);

    /* =========================================================
       PERSONALIZED RECOMMENDATIONS
       ========================================================= */

    /**
     * Fetch personalized recommendations for a user.
     */
    public Response getPersonalized(
            String accessToken,
            String surface,
            int limit
    ) {

        log.info("🎯 Fetching personalized recommendations surface={} limit={}",
                surface, limit);

        Response response =
                get(
                        authenticated(accessToken),
                        "/recommendations/personalized"
                                + "?surface=" + surface
                                + "&limit=" + limit
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       CONTEXTUAL RECOMMENDATIONS
       ========================================================= */

    /**
     * Fetch contextual recommendations (e.g. search, lookup, call screen).
     */
    public Response getContextual(
            String accessToken,
            String contextType,
            Map<String, Object> context,
            int limit
    ) {

        log.info("📍 Fetching contextual recommendations type={} limit={}",
                contextType, limit);

        Response response =
                post(
                        authenticated(accessToken),
                        "/recommendations/contextual",
                        Map.of(
                                "contextType", contextType,
                                "context", context,
                                "limit", limit
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       EXPERIMENT / VARIANT SUPPORT
       ========================================================= */

    /**
     * Fetch recommendations with an explicit experiment override.
     */
    public Response getWithExperiment(
            String accessToken,
            String surface,
            String experimentKey,
            String variant,
            int limit
    ) {

        log.info("🧪 Fetching recommendations surface={} experiment={} variant={}",
                surface, experimentKey, variant);

        Response response =
                post(
                        authenticated(accessToken),
                        "/recommendations/experiment",
                        Map.of(
                                "surface", surface,
                                "experimentKey", experimentKey,
                                "variant", variant,
                                "limit", limit
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       FALLBACK / DEFAULT
       ========================================================= */

    /**
     * Fetch fallback (non-personalized) recommendations.
     */
    public Response getFallback(
            String surface,
            int limit
    ) {

        log.info("🪜 Fetching fallback recommendations surface={} limit={}",
                surface, limit);

        Response response =
                get(
                        unauthenticated(),
                        "/recommendations/fallback"
                                + "?surface=" + surface
                                + "&limit=" + limit
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADMIN / DEBUG
       ========================================================= */

    /**
     * Fetch raw recommendation signals (admin/debug only).
     */
    public Response adminDebugSignals(
            String userId,
            String surface
    ) {

        log.info("🛠 Fetching recommendation debug signals user={} surface={}",
                userId, surface);

        Response response =
                get(
                        admin(),
                        "/admin/recommendations/debug"
                                + "?userId=" + userId
                                + "&surface=" + surface
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertRecommendationsPresent(Response response) {

        List<Map<String, Object>> items =
                response.path("items");

        if (items == null || items.isEmpty()) {
            fail("Expected recommendations but none were returned");
        }
    }

    public static void assertMaxResults(
            Response response,
            int max
    ) {

        List<Map<String, Object>> items =
                response.path("items");

        if (items != null && items.size() > max) {
            fail("Returned more recommendations than expected: "
                    + items.size());
        }
    }

    public static void assertVariant(
            Response response,
            String expectedVariant
    ) {

        String variant = response.path("experiment.variant");

        if (!expectedVariant.equals(variant)) {
            fail("Expected variant=" + expectedVariant
                    + " but got=" + variant);
        }
    }
}
