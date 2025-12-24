package com.company.qa.unified.utils;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

/**
 * ApiSchemaValidator
 *
 * Validates API request / response payloads against JSON or OpenAPI schemas.
 *
 * Used by:
 * - API schema tests
 * - Contract tests
 * - Regression checks
 * - Backward compatibility validation
 *
 * RULES:
 * ❌ Do not suppress validation errors
 * ✅ Fail fast with clear messages
 */
public final class ApiSchemaValidator {

    private static final Log log =
            Log.get(ApiSchemaValidator.class);

    private ApiSchemaValidator() {
        // utility class
    }

    /* =========================================================
       JSON SCHEMA VALIDATION
       ========================================================= */

    /**
     * Validate JSON payload against schema file on classpath.
     *
     * @param schemaClasspath e.g. "openapi/user-schema.json"
     * @param jsonPayload     actual request/response JSON
     */
    public static void validateJsonSchema(
            String schemaClasspath,
            String jsonPayload
    ) {

        log.info("📐 Validating JSON schema: {}", schemaClasspath);

        try (InputStream schemaStream =
                     getResource(schemaClasspath)) {

            JSONObject rawSchema =
                    new JSONObject(new JSONTokener(schemaStream));

            Schema schema =
                    SchemaLoader.load(rawSchema);

            schema.validate(new JSONObject(jsonPayload));

            log.info("✅ Schema validation passed");

        } catch (Exception e) {
            throw new AssertionError(
                    "❌ Schema validation failed for: " + schemaClasspath +
                            "\nPayload:\n" + jsonPayload,
                    e
            );
        }
    }

    /* =========================================================
       OPENAPI RESPONSE VALIDATION (SIMPLIFIED)
       ========================================================= */

    /**
     * Validate API response using extracted OpenAPI response schema.
     *
     * NOTE:
     * This expects a pre-extracted response schema JSON
     * (recommended for CI stability).
     */
    public static void validateOpenApiResponse(
            String responseSchemaPath,
            String responseBody
    ) {

        log.info("📘 Validating OpenAPI response schema: {}",
                responseSchemaPath);

        validateJsonSchema(responseSchemaPath, responseBody);
    }

    /* =========================================================
       NEGATIVE VALIDATION
       ========================================================= */

    /**
     * Assert payload is INVALID against schema.
     */
    public static void assertInvalidSchema(
            String schemaClasspath,
            String jsonPayload
    ) {

        try {
            validateJsonSchema(schemaClasspath, jsonPayload);
        } catch (AssertionError e) {
            log.info("✅ Payload correctly rejected by schema");
            return;
        }

        throw new AssertionError(
                "❌ Payload unexpectedly matched schema\n" + jsonPayload
        );
    }

    /* =========================================================
       INTERNAL HELPERS
       ========================================================= */

    private static InputStream getResource(String path) {
        InputStream stream =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(path);

        if (stream == null) {
            throw new IllegalStateException(
                    "Schema not found on classpath: " + path
            );
        }
        return stream;
    }
}
