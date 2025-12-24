package com.company.qa.unified.pages.api;

import com.company.qa.unified.drivers.APIClientFactory;
import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * BaseApiClient
 *
 * Base class for all API clients.
 *
 * Responsibilities:
 * - Provide standard HTTP methods
 * - Centralize logging
 * - Normalize error handling
 * - Allow authenticated & unauthenticated calls
 *
 * All concrete API classes SHOULD extend this class.
 */
public abstract class BaseApiClient {

    protected final Log log = Log.get(this.getClass());

    /* =========================================================
       REQUEST SPEC SELECTION
       ========================================================= */

    protected RequestSpecification unauthenticated() {
        return APIClientFactory.defaultClient();
    }

    protected RequestSpecification authenticated(String accessToken) {
        return APIClientFactory.customClient(
                Map.of("Authorization", "Bearer " + accessToken)
        );
    }

    protected RequestSpecification admin() {
        return APIClientFactory.adminClient();
    }

    /* =========================================================
       HTTP HELPERS
       ========================================================= */

    protected Response get(
            RequestSpecification spec,
            String path
    ) {
        return execute(spec, HttpMethod.GET, path, null);
    }

    protected Response post(
            RequestSpecification spec,
            String path,
            Object body
    ) {
        return execute(spec, HttpMethod.POST, path, body);
    }

    protected Response put(
            RequestSpecification spec,
            String path,
            Object body
    ) {
        return execute(spec, HttpMethod.PUT, path, body);
    }

    protected Response delete(
            RequestSpecification spec,
            String path
    ) {
        return execute(spec, HttpMethod.DELETE, path, null);
    }

    /* =========================================================
       CORE EXECUTION
       ========================================================= */

    private Response execute(
            RequestSpecification spec,
            HttpMethod method,
            String path,
            Object body
    ) {

        try {
            log.debug("➡️ {} {}", method, path);

            RequestSpecification request =
                    spec.when();

            if (body != null) {
                request = request.body(body);
            }

            Response response = switch (method) {
                case GET -> request.get(path);
                case POST -> request.post(path);
                case PUT -> request.put(path);
                case DELETE -> request.delete(path);
            };

            log.debug("⬅️ Response status={} body={}",
                    response.statusCode(),
                    truncate(response.asString())
            );

            return response;

        } catch (Exception e) {
            fail("API call failed [" + method + " " + path + "]: "
                    + e.getMessage());
            return null; // unreachable
        }
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    protected void assertStatus(
            Response response,
            int expectedStatus
    ) {
        if (response.statusCode() != expectedStatus) {
            fail("""
                 Unexpected API status
                 Expected: %d
                 Actual  : %d
                 Body    : %s
                 """.formatted(
                    expectedStatus,
                    response.statusCode(),
                    response.asString()
            ));
        }
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    private String truncate(String body) {
        if (body == null) return "";
        return body.length() > 500
                ? body.substring(0, 500) + "..."
                : body;
    }

    /* =========================================================
       HTTP METHOD ENUM
       ========================================================= */

    private enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }
}
