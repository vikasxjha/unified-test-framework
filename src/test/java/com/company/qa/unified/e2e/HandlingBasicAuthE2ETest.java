package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.BasicAuthUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-End test for HTTP Basic Authentication handling.
 *
 * Scenario:
 * - Create authenticated browser context
 * - Navigate to Basic Auth protected page
 * - Verify successful authentication
 */
public class HandlingBasicAuthE2ETest {

    @Test
    public void basicAuthProtectedPageIsAccessible() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            // 1️⃣ Create authenticated context
            BrowserContext context = BasicAuthUtil.createAuthenticatedContext(
                    browser,
                    "admin",
                    "admin"
            );

            Page page = context.newPage();

            // 2️⃣ Navigate to protected resource
            page.navigate("http://the-internet.herokuapp.com/basic_auth");

            // 3️⃣ Validate successful authentication
            String pageContent = page.textContent("body");

            assertThat(pageContent)
                    .as("Basic Auth success message")
                    .contains("Congratulations!");

            context.close();
            browser.close();
        }
    }
}
