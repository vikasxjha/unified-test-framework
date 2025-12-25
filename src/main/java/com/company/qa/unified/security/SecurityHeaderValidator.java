package com.company.qa.unified.security;

import com.company.qa.unified.utils.Log;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityHeaderValidator
 *
 * Simplified wrapper for security header validation in step definitions.
 */
public class SecurityHeaderValidator {

    private static final Log log = Log.get(SecurityHeaderValidator.class);

    /**
     * Validate Strict-Transport-Security header.
     */
    public void validateStrictTransportSecurity(Map<String, String> headers) {
        log.info("🔒 Validating HSTS header");

        String headerKey = findHeaderKey(headers, "strict-transport-security");

        assertNotNull(headerKey, "Strict-Transport-Security header not found");

        String value = headers.get(headerKey);
        assertTrue(
            value.contains("max-age"),
            "HSTS header must contain max-age"
        );

        log.info("✅ HSTS header valid: {}", value);
    }

    /**
     * Validate Content-Security-Policy header.
     */
    public void validateContentSecurityPolicy(Map<String, String> headers) {
        log.info("🔒 Validating CSP header");

        String headerKey = findHeaderKey(headers, "content-security-policy");

        assertNotNull(headerKey, "Content-Security-Policy header not found");

        String value = headers.get(headerKey);
        assertFalse(value.isEmpty(), "CSP header must not be empty");

        log.info("✅ CSP header valid");
    }

    /**
     * Validate X-Frame-Options header.
     */
    public void validateXFrameOptions(Map<String, String> headers) {
        log.info("🔒 Validating X-Frame-Options header");

        String headerKey = findHeaderKey(headers, "x-frame-options");

        assertNotNull(headerKey, "X-Frame-Options header not found");

        String value = headers.get(headerKey);
        assertTrue(
            value.equalsIgnoreCase("DENY") || value.equalsIgnoreCase("SAMEORIGIN"),
            "X-Frame-Options must be DENY or SAMEORIGIN"
        );

        log.info("✅ X-Frame-Options header valid: {}", value);
    }

    /**
     * Validate X-Content-Type-Options header.
     */
    public void validateXContentTypeOptions(Map<String, String> headers) {
        log.info("🔒 Validating X-Content-Type-Options header");

        String headerKey = findHeaderKey(headers, "x-content-type-options");

        assertNotNull(headerKey, "X-Content-Type-Options header not found");

        String value = headers.get(headerKey);
        assertEquals(
            "nosniff",
            value.toLowerCase(),
            "X-Content-Type-Options must be nosniff"
        );

        log.info("✅ X-Content-Type-Options header valid");
    }

    /**
     * Find header key case-insensitively.
     */
    private String findHeaderKey(Map<String, String> headers, String targetHeader) {
        return headers.keySet().stream()
            .filter(key -> key.equalsIgnoreCase(targetHeader))
            .findFirst()
            .orElse(null);
    }
}

