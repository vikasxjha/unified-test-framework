package com.company.qa.unified.security;

import java.util.List;
import java.util.Map;

/**
 * SecurityTestPayloads
 *
 * Central repository of security-related payloads for:
 * - SQL Injection
 * - XSS
 * - Command Injection
 * - Path Traversal
 * - Template Injection
 * - Header Injection
 * - Auth / JWT tampering
 *
 * Usage:
 * - API negative tests
 * - Web form security tests
 * - Mobile deep-link validation
 * - Event ingestion hardening
 *
 * NOTE:
 * These payloads are for TESTING ONLY.
 */
public final class SecurityTestPayloads {

    private SecurityTestPayloads() {
        // utility class
    }

    /* =========================================================
       SQL INJECTION
       ========================================================= */

    public static List<String> sqlInjection() {
        return List.of(
                "' OR '1'='1",
                "' OR 1=1 --",
                "'; DROP TABLE users; --",
                "\" OR \"\"=\"",
                "admin'--",
                "' UNION SELECT null, null --"
        );
    }

    /* =========================================================
       XSS (CROSS-SITE SCRIPTING)
       ========================================================= */

    public static List<String> xss() {
        return List.of(
                "<script>alert('xss')</script>",
                "<img src=x onerror=alert(1)>",
                "<svg/onload=alert(1)>",
                "<iframe src=javascript:alert(1)>",
                "<body onload=alert('xss')>"
        );
    }

    /* =========================================================
       COMMAND INJECTION
       ========================================================= */

    public static List<String> commandInjection() {
        return List.of(
                "; ls -la",
                "&& cat /etc/passwd",
                "| whoami",
                "`id`",
                "$(reboot)"
        );
    }

    /* =========================================================
       PATH TRAVERSAL
       ========================================================= */

    public static List<String> pathTraversal() {
        return List.of(
                "../",
                "../../etc/passwd",
                "..\\..\\windows\\system32",
                "%2e%2e%2f",
                "..%252f..%252f"
        );
    }

    /* =========================================================
       TEMPLATE INJECTION
       ========================================================= */

    public static List<String> templateInjection() {
        return List.of(
                "{{7*7}}",
                "${7*7}",
                "#{7*7}",
                "<%= 7 * 7 %>",
                "${{7*7}}"
        );
    }

    /* =========================================================
       HEADER INJECTION
       ========================================================= */

    public static Map<String, String> headerInjection() {
        return Map.of(
                "X-Forwarded-For", "127.0.0.1",
                "X-Original-URL", "/admin",
                "X-Host", "evil.com",
                "X-Api-Version", "999"
        );
    }

    /* =========================================================
       AUTH / JWT TAMPERING
       ========================================================= */

    public static List<String> invalidJwtTokens() {
        return List.of(
                "eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiJ9.",
                "invalid.token.value",
                "Bearer null",
                "Bearer ",
                "Bearer eyJhbGciOiJIUzI1NiJ9.e30.invalid"
        );
    }

    public static Map<String, String> authBypassHeaders() {
        return Map.of(
                "Authorization", "Bearer admin",
                "X-User-Role", "admin",
                "X-Authenticated", "true"
        );
    }

    /* =========================================================
       RATE LIMIT / DOS
       ========================================================= */

    public static List<String> highFrequencyPayloads() {
        return List.of(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "999999999999999999999999999999999999",
                "0".repeat(10_000)
        );
    }

    /* =========================================================
       FILE UPLOAD ATTACKS
       ========================================================= */

    public static List<String> maliciousFileNames() {
        return List.of(
                "shell.jsp",
                "shell.php",
                "payload.exe",
                "image.jpg.php",
                "../../shell.sh"
        );
    }

    /* =========================================================
       JSON STRUCTURE ATTACKS
       ========================================================= */

    public static Map<String, Object> malformedJson() {
        return Map.of(
                "user", Map.of(
                        "name", "<script>alert(1)</script>",
                        "roles", List.of("admin", "' OR 1=1 --")
                ),
                "unexpected", Map.of(
                        "$ref", "http://evil.com/schema"
                )
        );
    }
}
