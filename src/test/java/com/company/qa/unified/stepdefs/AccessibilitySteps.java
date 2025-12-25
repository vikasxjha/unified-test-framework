package com.company.qa.unified.stepdefs;

import com.company.qa.unified.accessibility.AxeCoreScanner;
import com.company.qa.unified.drivers.PlaywrightDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessibilitySteps
 *
 * Cucumber step definitions for accessibility testing (axe-core).
 *
 * Covers:
 * - WCAG 2.1 Level A/AA compliance
 * - Critical violations
 * - Color contrast
 * - Keyboard navigation
 * - Screen reader compatibility
 */
public class AccessibilitySteps {

    private static final Log log = Log.get(AccessibilitySteps.class);

    private AxeCoreScanner axeScanner;
    private List<Map<String, Object>> violations;

    /* =========================================================
       ACCESSIBILITY SCANNING
       ========================================================= */

    @Given("accessibility scanning is enabled")
    public void enableAccessibilityScanning() {
        log.info("♿ Enabling accessibility scanning");

        axeScanner = new AxeCoreScanner();

        ReportPublisher.step("Accessibility scanning enabled");
    }

    @Then("the home page should have no critical accessibility violations")
    public void verifyNoCriticalA11yViolations() {
        log.info("✅ Verifying no critical accessibility violations");

        Page page = PlaywrightDriverFactory.getPage();

        if (axeScanner == null) {
            axeScanner = new AxeCoreScanner();
        }

        // Inject axe-core and run scan
        violations = axeScanner.scan(page);

        // Filter critical violations
        long criticalCount = violations.stream()
            .filter(v -> "critical".equals(v.get("impact")))
            .count();

        assertEquals(
            0,
            criticalCount,
            "Found " + criticalCount + " critical accessibility violations"
        );

        ReportPublisher.step("No critical accessibility violations found");

        if (!violations.isEmpty()) {
            ReportPublisher.attachJson("A11y Violations", violations.toString());
        }
    }

    @Then("the page should have no serious accessibility violations")
    public void verifyNoSeriousA11yViolations() {
        log.info("✅ Verifying no serious accessibility violations");

        if (violations == null) {
            Page page = PlaywrightDriverFactory.getPage();
            violations = axeScanner.scan(page);
        }

        // Filter serious violations
        long seriousCount = violations.stream()
            .filter(v -> "serious".equals(v.get("impact")))
            .count();

        assertEquals(
            0,
            seriousCount,
            "Found " + seriousCount + " serious accessibility violations"
        );

        ReportPublisher.step("No serious accessibility violations found");
    }

    @Then("the page should be WCAG {string} compliant")
    public void verifyWcagCompliance(String level) {
        log.info("✅ Verifying WCAG {} compliance", level);

        Page page = PlaywrightDriverFactory.getPage();

        if (axeScanner == null) {
            axeScanner = new AxeCoreScanner();
        }

        // Run scan with specific WCAG level
        violations = axeScanner.scanForWcagLevel(page, level);

        assertTrue(
            violations.isEmpty(),
            "Found " + violations.size() + " WCAG " + level + " violations"
        );

        ReportPublisher.step("Page is WCAG " + level + " compliant");
    }

    @Then("all images should have alt text")
    public void verifyAllImagesHaveAltText() {
        log.info("✅ Verifying all images have alt text");

        Page page = PlaywrightDriverFactory.getPage();

        if (axeScanner == null) {
            axeScanner = new AxeCoreScanner();
        }

        boolean hasAltTextViolations = axeScanner.checkImageAltText(page);

        assertFalse(
            hasAltTextViolations,
            "Some images are missing alt text"
        );

        ReportPublisher.step("All images have alt text");
    }

    @Then("color contrast should meet WCAG standards")
    public void verifyColorContrast() {
        log.info("✅ Verifying color contrast");

        Page page = PlaywrightDriverFactory.getPage();

        if (axeScanner == null) {
            axeScanner = new AxeCoreScanner();
        }

        boolean hasContrastViolations = axeScanner.checkColorContrast(page);

        assertFalse(
            hasContrastViolations,
            "Color contrast violations detected"
        );

        ReportPublisher.step("Color contrast meets WCAG standards");
    }
}

