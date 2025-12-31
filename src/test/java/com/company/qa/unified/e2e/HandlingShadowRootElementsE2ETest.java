package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.ShadowDomUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for Shadow DOM handling using Playwright.
 *
 * Scenario:
 * - Navigate to Shadow DOM based application
 * - Interact with elements inside Shadow Root
 * - Validate results
 */
public class HandlingShadowRootElementsE2ETest {

    @Test
    public void shadowDomElementsCanBeInteractedWith() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate to Shadow DOM demo site
            page.navigate("https://books-pwakit.appspot.com/");
            assertThat(page.title()).contains("Books");

            /*
             * Shadow DOM structure (simplified):
             *
             * <book-app>  <-- shadow root
             *   <app-header> <-- shadow root
             *     <input id="input">
             */

            // 2️⃣ Type inside Shadow DOM input
            ShadowDomUtil.type(
                    page,
                    "book-app >>> app-header >>> input#input",
                    "java"
            );

            // 3️⃣ Validate search results appear
            String resultsText = ShadowDomUtil.getText(
                    page,
                    "book-app >>> book-list"
            );

            assertThat(resultsText.toLowerCase())
                    .contains("java");

            browser.close();
        }
    }
}
