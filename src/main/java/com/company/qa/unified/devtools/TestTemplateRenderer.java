package com.company.qa.unified.devtools;

import com.company.qa.unified.utils.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight template rendering engine for test scaffolding.
 *
 * Supported features:
 * - {{placeholders}} replacement
 * - Safe rendering (fails on missing variables)
 * - File-based or inline templates
 *
 * This intentionally avoids heavy template engines
 * (Freemarker/Velocity) to keep tooling simple.
 */
public final class TestTemplateRenderer {

    private static final Log log =
            Log.get(TestTemplateRenderer.class);

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{\\{(.+?)}}");

    private TestTemplateRenderer() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    /**
     * Render a template string using provided variables.
     */
    public static String render(
            String template,
            Map<String, String> variables
    ) {
        Objects.requireNonNull(template, "template cannot be null");
        Objects.requireNonNull(variables, "variables cannot be null");

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1).trim();

            if (!variables.containsKey(key)) {
                throw new IllegalStateException(
                        "Missing template variable: " + key);
            }

            String replacement =
                    Matcher.quoteReplacement(
                            variables.get(key)
                    );

            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Render a template file into a destination file.
     */
    public static void renderToFile(
            Path templatePath,
            Path outputPath,
            Map<String, String> variables
    ) throws IOException {

        if (!Files.exists(templatePath)) {
            throw new IllegalStateException(
                    "Template file not found: " + templatePath);
        }

        String template =
                Files.readString(templatePath, StandardCharsets.UTF_8);

        String rendered = render(template, variables);

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);

        log.info("Rendered template {} → {}",
                templatePath, outputPath);
    }

    /**
     * Render inline template and write to file only if absent.
     */
    public static void renderIfAbsent(
            Path outputPath,
            String template,
            Map<String, String> variables
    ) throws IOException {

        if (Files.exists(outputPath)) {
            log.warn("⚠️ File already exists: {}", outputPath);
            return;
        }

        String rendered = render(template, variables);

        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, rendered, StandardCharsets.UTF_8);

        log.info("Created file from template: {}", outputPath);
    }

    /* =========================================================
       DEBUGGING / VALIDATION
       ========================================================= */

    /**
     * Validate that all placeholders in a template
     * have corresponding variables.
     */
    public static void validateTemplate(
            String template,
            Map<String, String> variables
    ) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            if (!variables.containsKey(key)) {
                throw new IllegalStateException(
                        "Template validation failed. Missing key: " + key);
            }
        }
    }
}
