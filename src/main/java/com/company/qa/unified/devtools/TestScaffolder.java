package com.company.qa.unified.devtools;

import com.company.qa.unified.utils.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

/**
 * TestScaffolder generates boilerplate test artifacts
 * following org conventions.
 *
 * Generates:
 * - Feature files
 * - Step definitions
 * - E2E test classes
 * - Page Objects / API clients (stubs)
 *
 * This tool is intended to be run by engineers,
 * not during CI.
 */
public final class TestScaffolder {

    private static final Log log =
            Log.get(TestScaffolder.class);

    private static final String BASE_TEST_PKG =
            "src/test/java/com/company/qa/unified";

    private static final String BASE_FEATURES =
            "src/test/java/features";

    private static final String BASE_PAGES =
            "src/main/java/com/company/qa/unified/pages";

    private TestScaffolder() {
        // utility
    }

    /* =========================================================
       PUBLIC ENTRY POINT
       ========================================================= */

    /**
     * Scaffold a full E2E test bundle.
     *
     * Example:
     *   TestScaffolder.createE2ETest("UserUpgradeToPremium");
     */
    public static void createE2ETest(String name) {
        String normalized = normalize(name);

        try {
            createFeature(normalized);
            createStepDef(normalized);
            createE2ETestClass(normalized);

            log.info("✅ E2E test scaffold created for {}", normalized);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to scaffold E2E test: " + name, e);
        }
    }

    /* =========================================================
       FEATURE FILE
       ========================================================= */

    private static void createFeature(String name)
            throws IOException {

        Path path = Path.of(
                BASE_FEATURES,
                toSnake(name) + ".feature"
        );

        writeIfAbsent(path,
                """
                @e2e
                Feature: %s

                  Scenario: %s happy path
                    Given a valid user exists
                    When the user performs %s
                    Then the operation should succeed
                """.formatted(
                        splitCamel(name),
                        splitCamel(name),
                        splitCamel(name)
                )
        );
    }

    /* =========================================================
       STEP DEFINITIONS
       ========================================================= */

    private static void createStepDef(String name)
            throws IOException {

        Path path = Path.of(
                BASE_TEST_PKG,
                "stepdefs",
                name + "Steps.java"
        );

        writeIfAbsent(path,
                """
                package com.company.qa.unified.stepdefs;

                import io.cucumber.java.en.*;
                import com.company.qa.unified.utils.Log;

                public class %sSteps {

                    private static final Log log =
                            Log.get(%sSteps.class);

                    @Given("a valid user exists")
                    public void userExists() {
                        log.info("User exists");
                    }

                    @When("the user performs %s")
                    public void userAction() {
                        log.info("User performs %s");
                    }

                    @Then("the operation should succeed")
                    public void verifySuccess() {
                        log.info("Operation succeeded");
                    }
                }
                """.formatted(
                        name,
                        name,
                        splitCamel(name),
                        splitCamel(name)
                )
        );
    }

    /* =========================================================
       E2E TEST CLASS
       ========================================================= */

    private static void createE2ETestClass(String name)
            throws IOException {

        Path path = Path.of(
                BASE_TEST_PKG,
                "e2e",
                name + "E2ETest.java"
        );

        writeIfAbsent(path,
                """
                package com.company.qa.unified.e2e;

                import org.testng.annotations.Test;
                import com.company.qa.unified.utils.Log;

                /**
                 * Auto-generated E2E test.
                 *
                 * Created on %s
                 */
                public class %sE2ETest {

                    private static final Log log =
                            Log.get(%sE2ETest.class);

                    @Test
                    public void %sHappyPath() {
                        log.info("Executing %s E2E test");
                        // Orchestrate steps here if needed
                    }
                }
                """.formatted(
                        LocalDate.now(),
                        name,
                        name,
                        toCamelLower(name),
                        splitCamel(name)
                )
        );
    }

    /* =========================================================
       OPTIONAL PAGE / API STUBS
       ========================================================= */

    public static void createWebPageStub(String pageName)
            throws IOException {

        Path path = Path.of(
                BASE_PAGES,
                "web",
                pageName + "Page.java"
        );

        writeIfAbsent(path,
                """
                package com.company.qa.unified.pages.web;

                public class %sPage {
                    // TODO: Add locators & actions
                }
                """.formatted(pageName)
        );
    }

    public static void createApiStub(String apiName)
            throws IOException {

        Path path = Path.of(
                BASE_PAGES,
                "api",
                apiName + "Api.java"
        );

        writeIfAbsent(path,
                """
                package com.company.qa.unified.pages.api;

                public class %sApi {
                    // TODO: Add API methods
                }
                """.formatted(apiName)
        );
    }

    /* =========================================================
       FILE HELPERS
       ========================================================= */

    private static void writeIfAbsent(Path path, String content)
            throws IOException {

        if (Files.exists(path)) {
            log.warn("⚠️ File already exists: {}", path);
            return;
        }

        Files.createDirectories(path.getParent());
        Files.writeString(path, content.trim());

        log.info("Created file: {}", path);
    }

    /* =========================================================
       STRING UTILITIES
       ========================================================= */

    private static String normalize(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "");
    }

    private static String splitCamel(String input) {
        return input.replaceAll(
                "([a-z])([A-Z])", "$1 $2");
    }

    private static String toSnake(String input) {
        return splitCamel(input)
                .toLowerCase(Locale.ROOT)
                .replace(" ", "_");
    }

    private static String toCamelLower(String input) {
        return Character.toLowerCase(input.charAt(0))
                + input.substring(1);
    }
}
