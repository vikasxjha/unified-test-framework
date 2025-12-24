package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * AccessibilityChecker
 *
 * Runs WCAG accessibility checks using axe-core via Playwright.
 *
 * Features:
 * - Page-level and selector-level scans
 * - Rule include/exclude
 * - Severity filtering
 * - CI-safe reporting (does not crash infra)
 *
 * Requirements:
 * - axe.min.js available on classpath (resources/axe/axe.min.js)
 */
public final class AccessibilityChecker {

    private static final Log log =
            Log.get(AccessibilityChecker.class);

    private static final String AXE_SCRIPT_PATH =
            "axe/axe.min.js";

    private AccessibilityChecker() {
        // utility class
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Run full page accessibility scan.
     */
    public static void assertNoViolations(Page page) {
        AxeResult result = runAxe(page, null, null, null);
        assertResult(result);
    }

    /**
     * Run accessibility scan for a specific selector.
     */
    public static void assertNoViolations(
            Page page,
            String cssSelector
    ) {
        AxeResult result =
                runAxe(page, cssSelector, null, null);
        assertResult(result);
    }

    /**
     * Run accessibility scan with rule filtering.
     */
    public static void assertNoViolations(
            Page page,
            String cssSelector,
            List<String> includeRules,
            List<String> excludeRules
    ) {
        AxeResult result =
                runAxe(page, cssSelector, includeRules, excludeRules);
        assertResult(result);
    }

    /* =========================================================
       CORE EXECUTION
       ========================================================= */

    private static AxeResult runAxe(
            Page page,
            String selector,
            List<String> includeRules,
            List<String> excludeRules
    ) {

        injectAxe(page);

        StringBuilder script = new StringBuilder();
        script.append("return axe.run(");

        if (selector != null) {
            script.append("document.querySelector('")
                  .append(selector)
                  .append("')");
        } else {
            script.append("document");
        }

        script.append(", {");

        if (includeRules != null && !includeRules.isEmpty()) {
            script.append("runOnly: { type: 'rule', values: ")
                  .append(toJsArray(includeRules))
                  .append(" },");
        }

        if (excludeRules != null && !excludeRules.isEmpty()) {
            script.append("rules: {");
            for (String rule : excludeRules) {
                script.append("'")
                      .append(rule)
                      .append("': { enabled: false },");
            }
            script.append("},");
        }

        script.append("});");

        @SuppressWarnings("unchecked")
        Map<String, Object> raw =
                (Map<String, Object>) page.evaluate(script.toString());

        return AxeResult.from(raw);
    }

    /* =========================================================
       ASSERTION & REPORTING
       ========================================================= */

    private static void assertResult(AxeResult result) {

        ReportPublisher.attachJson(
                "Accessibility Report",
                result.toPrettyJson()
        );

        if (!result.violations.isEmpty()) {
            String message =
                    "Accessibility violations found: "
                            + result.violations.size();

            log.error(message);
            throw new AssertionError(message + "\n" + result.toPrettyText());
        }

        log.info("✅ Accessibility check passed (no violations)");
    }

    /* =========================================================
       AXE INJECTION
       ========================================================= */

    private static void injectAxe(Page page) {

        if (Boolean.TRUE.equals(
                page.evaluate("() => window.axe !== undefined"))) {
            return;
        }

        try (InputStream is =
                     Thread.currentThread()
                           .getContextClassLoader()
                           .getResourceAsStream(AXE_SCRIPT_PATH)) {

            if (is == null) {
                throw new IllegalStateException(
                        "axe.min.js not found at " + AXE_SCRIPT_PATH);
            }

            String axeScript =
                    new String(is.readAllBytes(), StandardCharsets.UTF_8);

            page.addInitScript(axeScript);
            page.evaluate(axeScript);

            log.info("♿ axe-core injected");

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to inject axe-core", e);
        }
    }

    /* =========================================================
       UTIL
       ========================================================= */

    private static String toJsArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (String v : values) {
            sb.append("'").append(v).append("',");
        }
        sb.append("]");
        return sb.toString();
    }

    /* =========================================================
       RESULT MODEL
       ========================================================= */

    private static class AxeResult {

        final List<Map<String, Object>> violations;
        final List<Map<String, Object>> passes;

        private AxeResult(
                List<Map<String, Object>> violations,
                List<Map<String, Object>> passes
        ) {
            this.violations = violations;
            this.passes = passes;
        }

        @SuppressWarnings("unchecked")
        static AxeResult from(Map<String, Object> raw) {
            return new AxeResult(
                    (List<Map<String, Object>>) raw.get("violations"),
                    (List<Map<String, Object>>) raw.get("passes")
            );
        }

        String toPrettyJson() {
            return JsonUtils.toPrettyJson(
                    Map.of(
                            "violations", violations,
                            "passes", passes
                    )
            );
        }

        String toPrettyText() {
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> v : violations) {
                sb.append("Rule: ").append(v.get("id")).append("\n")
                  .append("Impact: ").append(v.get("impact")).append("\n")
                  .append("Help: ").append(v.get("help")).append("\n\n");
            }
            return sb.toString();
        }
    }
}
