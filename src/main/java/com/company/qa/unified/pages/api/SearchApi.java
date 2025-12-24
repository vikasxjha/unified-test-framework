package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * SearchApi
 *
 * Encapsulates search-related APIs.
 *
 * Covers:
 * - Keyword search
 * - Advanced filters
 * - Pagination & sorting
 * - Spell-correction / suggestions
 * - Personalized search
 * - Cache validation
 * - Admin debug endpoints
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use SearchApi
 */
public class SearchApi extends BaseApiClient {

    private static final Log log =
            Log.get(SearchApi.class);

    /* =========================================================
       BASIC SEARCH
       ========================================================= */

    /**
     * Perform a basic keyword search.
     */
    public Response search(
            String accessToken,
            String query,
            int limit,
            int offset
    ) {

        log.info("🔍 Search query='{}' limit={} offset={}",
                query, limit, offset);

        Response response =
                get(
                        authenticated(accessToken),
                        "/search?q=" + query
                                + "&limit=" + limit
                                + "&offset=" + offset
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Anonymous search (limited results).
     */
    public Response anonymousSearch(
            String query,
            int limit
    ) {

        log.info("🔍 Anonymous search query='{}'", query);

        Response response =
                get(
                        unauthenticated(),
                        "/search/anonymous?q=" + query
                                + "&limit=" + limit
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ADVANCED SEARCH
       ========================================================= */

    /**
     * Search with filters (spam, verified, category, etc.).
     */
    public Response searchWithFilters(
            String accessToken,
            String query,
            Map<String, Object> filters,
            int limit
    ) {

        log.info("🔍 Filtered search query='{}' filters={}",
                query, filters.keySet());

        Response response =
                post(
                        authenticated(accessToken),
                        "/search/advanced",
                        Map.of(
                                "query", query,
                                "filters", filters,
                                "limit", limit
                        )
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PERSONALIZED SEARCH
       ========================================================= */

    /**
     * Search with personalization enabled.
     */
    public Response personalizedSearch(
            String accessToken,
            String query,
            int limit
    ) {

        log.info("🎯 Personalized search query='{}'", query);

        Response response =
                get(
                        authenticated(accessToken),
                        "/search/personalized?q=" + query
                                + "&limit=" + limit
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       SPELL-CORRECTION & SUGGESTIONS
       ========================================================= */

    /**
     * Fetch spell-correction / query suggestions.
     */
    public Response suggestions(
            String query
    ) {

        log.info("✍️ Fetching suggestions for query='{}'", query);

        Response response =
                get(
                        unauthenticated(),
                        "/search/suggestions?q=" + query
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       CACHE / CONDITIONAL REQUESTS
       ========================================================= */

    /**
     * Perform search with ETag to validate caching.
     */
    public Response searchWithEtag(
            String accessToken,
            String query,
            String etag
    ) {

        log.info("🗄 Search with ETag query='{}'", query);

        Response response =
                APIClientFactory
                        .customClient(Map.of(
                                "Authorization",
                                "Bearer " + accessToken,
                                "If-None-Match",
                                etag
                        ))
                        .get("/search?q=" + query);

        if (response.statusCode() != 200 &&
                response.statusCode() != 304) {

            fail("Unexpected status for cached search: "
                    + response.statusCode());
        }

        return response;
    }

    /* =========================================================
       ADMIN / DEBUG
       ========================================================= */

    /**
     * Fetch raw search signals (ranking, scoring, boosts).
     */
    public Response adminDebugSearch(
            String query
    ) {

        log.info("🛠 Admin debug search query='{}'", query);

        Response response =
                get(
                        admin(),
                        "/admin/search/debug?q=" + query
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertResultsPresent(Response response) {

        List<Map<String, Object>> results =
                response.path("results");

        if (results == null || results.isEmpty()) {
            fail("Expected search results but none returned");
        }
    }

    public static void assertMaxResults(
            Response response,
            int max
    ) {

        List<Map<String, Object>> results =
                response.path("results");

        if (results != null && results.size() > max) {
            fail("Search returned more results than expected: "
                    + results.size());
        }
    }

    public static void assertSuggestionPresent(
            Response response,
            String expected
    ) {

        List<String> suggestions =
                response.path("suggestions");

        if (suggestions == null ||
                !suggestions.contains(expected)) {

            fail("Expected suggestion '" + expected
                    + "' not found");
        }
    }
}
