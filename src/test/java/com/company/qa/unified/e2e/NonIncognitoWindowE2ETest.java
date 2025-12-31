package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.PersistentBrowserUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for NON-INCOGNITO (persistent) browser window.
 *
 * Scenario:
 * - Launch persistent browser profile
 * - Navigate across pages
 * - Use browser navigation (back, forward, reload)
 * - Validate page titles
 */
public class NonIncognitoWindowE2ETest {

    @Test
    public void persistentBrowserMaintainsSessionAndNavigation() {

        try (Playwright playwright = Playwright.create()) {

            // 1️⃣ Launch persistent browser context
            BrowserContext context = PersistentBrowserUtil.launchPersistentContext(
                    playwright,
                    "target/browser-profile",
                    null,          // use bundled Chromium
                    false
            );

            Page page = context.newPage();

            // 2️⃣ Navigate to site
            page.navigate("https://way2automation.com");
            String title1 = page.title();
            assertThat(title1).contains("Way2Automation");

            // 3️⃣ Navigate to another site
            page.navigate("https://www.google.com");
            String title2 = page.title();
            assertThat(title2).contains("Google");

            // 4️⃣ Go back
            page.goBack();
            assertThat(page.title()).contains("Way2Automation");

            // 5️⃣ Go forward
            page.goForward();
            assertThat(page.title()).contains("Google");

            // 6️⃣ Reload
            page.reload();
            assertThat(page.title()).contains("Google");

            // Cleanup
            page.close();
            context.close();
        }
    }
}
