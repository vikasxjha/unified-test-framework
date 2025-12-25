package com.company.qa.unified.stepdefs;

import com.company.qa.unified.drivers.PlaywrightDriverFactory;
import com.company.qa.unified.security.SecurityHeaderValidator;
import com.company.qa.unified.security.OwaspZapScanner;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecuritySteps
 *
 * Cucumber step definitions for security testing.
 *
 * Covers:
 * - Security headers
 * - OWASP ZAP scanning
 * - SSL/TLS validation
 * - XSS/CSRF checks
 */
public class SecuritySteps {

    private static final Log log = Log.get(SecuritySteps.class);

    private SecurityHeaderValidator headerValidator;
    private Response lastResponse;
    private OwaspZapScanner zapScanner;
    private List<Map<String, String>> securityIssues;

    /* =========================================================
       SECURITY HEADERS
       ========================================================= */

    @Then("the response should contain mandatory security headers")
    public void verifySecurityHeaders() {
        log.info("🔒 Verifying security headers");

        Page page = PlaywrightDriverFactory.getPage();

        // Get the last response from the page
        lastResponse = page.context().pages().get(0)
            .waitForLoadState()
            .navigate(page.url())
            .finished();

        headerValidator = new SecurityHeaderValidator();

        // Validate mandatory headers
        Map<String, String> headers = lastResponse.headers();

        headerValidator.validateStrictTransportSecurity(headers);
        headerValidator.validateContentSecurityPolicy(headers);
        headerValidator.validateXFrameOptions(headers);
        headerValidator.validateXContentTypeOptions(headers);

        ReportPublisher.step("Security headers validated");
        ReportPublisher.attachJson("Response Headers", headers.toString());
    }

    @Then("the response should have HSTS enabled")
    public void verifyHstsEnabled() {
        log.info("🔒 Verifying HSTS enabled");

        Page page = PlaywrightDriverFactory.getPage();
        Response response = page.context().pages().get(0)
            .waitForLoadState()
            .navigate(page.url())
            .finished();

        Map<String, String> headers = response.headers();

        assertTrue(
            headers.containsKey("strict-transport-security"),
            "HSTS header not found"
        );

        String hsts = headers.get("strict-transport-security");
        assertTrue(
            hsts.contains("max-age"),
            "HSTS header missing max-age"
        );

        ReportPublisher.step("HSTS validated: " + hsts);
    }

    @Then("the response should have CSP configured")
    public void verifyCspConfigured() {
        log.info("🔒 Verifying CSP configured");

        Page page = PlaywrightDriverFactory.getPage();
        Response response = page.context().pages().get(0)
            .waitForLoadState()
            .navigate(page.url())
            .finished();

        Map<String, String> headers = response.headers();

        assertTrue(
            headers.containsKey("content-security-policy") ||
            headers.containsKey("content-security-policy-report-only"),
            "CSP header not found"
        );

        ReportPublisher.step("CSP validated");
    }

    /* =========================================================
       OWASP ZAP SCANNING
       ========================================================= */

    @Given("OWASP ZAP scanner is configured")
    public void configureOwaspZap() {
        log.info("🛡️ Configuring OWASP ZAP scanner");

        zapScanner = new OwaspZapScanner();
        zapScanner.initialize();

        ReportPublisher.step("OWASP ZAP scanner configured");
    }

    @When("I run a security scan")
    public void runSecurityScan() {
        log.info("🔍 Running security scan");

        Page page = PlaywrightDriverFactory.getPage();
        String targetUrl = page.url();

        if (zapScanner == null) {
            configureOwaspZap();
        }

        securityIssues = zapScanner.scan(targetUrl);

        ReportPublisher.step("Security scan completed");
    }

    @Then("there should be no high severity vulnerabilities")
    public void verifyNoHighSeverityVulnerabilities() {
        log.info("✅ Verifying no high severity vulnerabilities");

        assertNotNull(securityIssues, "Security scan not performed");

        long highSeverityCount = securityIssues.stream()
            .filter(issue -> "HIGH".equals(issue.get("severity")))
            .count();

        assertEquals(
            0,
            highSeverityCount,
            "Found " + highSeverityCount + " high severity vulnerabilities"
        );

        ReportPublisher.step("No high severity vulnerabilities found");
        ReportPublisher.attachJson("Security Scan Results", securityIssues.toString());
    }

    @Then("there should be no critical vulnerabilities")
    public void verifyNoCriticalVulnerabilities() {
        log.info("✅ Verifying no critical vulnerabilities");

        assertNotNull(securityIssues, "Security scan not performed");

        long criticalCount = securityIssues.stream()
            .filter(issue -> "CRITICAL".equals(issue.get("severity")))
            .count();

        assertEquals(
            0,
            criticalCount,
            "Found " + criticalCount + " critical vulnerabilities"
        );

        ReportPublisher.step("No critical vulnerabilities found");
    }
}

