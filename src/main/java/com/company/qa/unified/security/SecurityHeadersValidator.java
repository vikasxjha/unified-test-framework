package com.company.qa.unified.security;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;

import java.net.http.HttpHeaders;
import java.util.*;

/**
 * SecurityHeadersValidator
 *
 * Validates presence and correctness of security headers for:
 * - Web responses
 * - API responses
 * - Reverse proxies / gateways
 *
 * Supports:
 * - Strict & Relaxed profiles
 * - Per-header value validation
 * - Clear reporting
 *
 * RULES:
 * ❌ Do not silently ignore missing headers
 * ✅ Fail with actionable error messages
 */
public final class SecurityHeadersValidator {

    private static final Log log =
            Log.get(SecurityHeadersValidator.class);

    private SecurityHeadersValidator() {
        // utility class
    }

    /* =========================================================
       PROFILES
       ========================================================= */

    public enum Profile {
        STRICT,
        RELAXED
    }

    /* =========================================================
       HEADER DEFINITIONS
       ========================================================= */

    private static final Map<String, HeaderRule> STRICT_HEADERS =
            Map.of(
                    "Content-Security-Policy",
                    HeaderRule.required(),

                    "X-Content-Type-Options",
                    HeaderRule.mustEqual("nosniff"),

                    "X-Frame-Options",
                    HeaderRule.oneOf("DENY", "SAMEORIGIN"),

                    "X-XSS-Protection",
                    HeaderRule.mustContain("1"),

                    "Referrer-Policy",
                    HeaderRule.any(),

                    "Strict-Transport-Security",
                    HeaderRule.mustContain("max-age"),

                    "Permissions-Policy",
                    HeaderRule.any()
            );

    private static final Map<String, HeaderRule> RELAXED_HEADERS =
            Map.of(
                    "X-Content-Type-Options",
                    HeaderRule.any(),

                    "X-Frame-Options",
                    HeaderRule.any(),

                    "Referrer-Policy",
                    HeaderRule.any()
            );

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Validate headers using a predefined profile.
     */
    public static void validate(
            HttpHeaders headers,
            Profile profile
    ) {

        log.info("🔐 Validating security headers using profile {}", profile);

        Map<String, List<String>> headerMap = headers.map();
        Map<String, HeaderRule> rules =
                profile == Profile.STRICT
                        ? STRICT_HEADERS
                        : RELAXED_HEADERS;

        List<String> violations = new ArrayList<>();

        for (Map.Entry<String, HeaderRule> entry : rules.entrySet()) {
            String headerName = entry.getKey();
            HeaderRule rule = entry.getValue();

            List<String> values = headerMap.get(headerName);

            if (values == null || values.isEmpty()) {
                violations.add("Missing header: " + headerName);
                continue;
            }

            if (!rule.validate(values)) {
                violations.add(
                        "Invalid value for header " + headerName +
                                ": " + values
                );
            }
        }

        ReportPublisher.attachText(
                "Security Headers Validation",
                buildReport(profile, headerMap, violations)
        );

        if (!violations.isEmpty()) {
            throw new AssertionError(
                    "Security header validation failed:\n" +
                            String.join("\n", violations)
            );
        }

        log.info("✅ Security headers validation passed");
    }

    /**
     * Validate headers using custom rules.
     */
    public static void validateCustom(
            HttpHeaders headers,
            Map<String, HeaderRule> rules
    ) {

        log.info("🔐 Validating security headers (custom rules)");

        List<String> violations = new ArrayList<>();

        for (Map.Entry<String, HeaderRule> entry : rules.entrySet()) {

            String headerName = entry.getKey();
            HeaderRule rule = entry.getValue();

            List<String> values = headers.allValues(headerName);

            if ((values == null || values.isEmpty()) && rule.required) {
                violations.add("Missing required header: " + headerName);
                continue;
            }

            if (values != null && !values.isEmpty()
                    && !rule.validate(values)) {
                violations.add(
                        "Invalid value for header " + headerName +
                                ": " + values
                );
            }
        }

        if (!violations.isEmpty()) {
            throw new AssertionError(
                    "Security header validation failed:\n" +
                            String.join("\n", violations)
            );
        }
    }

    /* =========================================================
       INTERNAL REPORT
       ========================================================= */

    private static String buildReport(
            Profile profile,
            Map<String, List<String>> headers,
            List<String> violations
    ) {

        StringBuilder sb = new StringBuilder();
        sb.append("Profile: ").append(profile).append("\n\n");
        sb.append("Headers:\n");

        headers.forEach((k, v) ->
                sb.append(k).append(": ").append(v).append("\n"));

        sb.append("\nViolations:\n");
        if (violations.isEmpty()) {
            sb.append("None");
        } else {
            violations.forEach(v -> sb.append("- ").append(v).append("\n"));
        }

        return sb.toString();
    }

    /* =========================================================
       HEADER RULE MODEL
       ========================================================= */

    public static class HeaderRule {

        private final boolean required;
        private final Set<String> allowedValues;
        private final String mustContain;

        private HeaderRule(
                boolean required,
                Set<String> allowedValues,
                String mustContain
        ) {
            this.required = required;
            this.allowedValues = allowedValues;
            this.mustContain = mustContain;
        }

        public static HeaderRule required() {
            return new HeaderRule(true, null, null);
        }

        public static HeaderRule any() {
            return new HeaderRule(true, null, null);
        }

        public static HeaderRule mustEqual(String value) {
            return new HeaderRule(true, Set.of(value), null);
        }

        public static HeaderRule oneOf(String... values) {
            return new HeaderRule(true, Set.of(values), null);
        }

        public static HeaderRule mustContain(String fragment) {
            return new HeaderRule(true, null, fragment);
        }

        boolean validate(List<String> values) {

            for (String value : values) {

                if (allowedValues != null
                        && allowedValues.contains(value)) {
                    return true;
                }

                if (mustContain != null
                        && value.contains(mustContain)) {
                    return true;
                }

                if (allowedValues == null && mustContain == null) {
                    return true; // any value accepted
                }
            }
            return false;
        }
    }
}
