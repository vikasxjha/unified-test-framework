package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.BrowserLaunchUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test validating browser launch configuration.
 *
 * Scenario:
 * - Launch browser with specific channel
 * - Create full screen context
 * - Navigate to application
 * - Validate page load
 */
public class LaunchBrowserE2ETest {

    @Test
    public void browserLaunchesWithFullScreenViewport() {

        try (Playwright playwright = Playwright.create()) {

            // 1️⃣ Launch browser (Edge via Chromium)
            Browser browser = BrowserLaunchUtil.launchBrowser(
                    playwright,
                    "chromium",
                    "msedge",
                    false
            );

            // 2️⃣ Create full screen context
            BrowserContext context =
                    BrowserLaunchUtil.createFullScreenContext(browser);

            Page page = context.newPage();

            // 3️⃣ Navigate
            page.navigate("https://way2automation.com");

            // 4️⃣ Validate page title
            assertThat(page.title())
                    .as("Page title validation")
                    .contains("Way2Automation");

            context.close();
            browser.close();
        }
    }
}
