package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.FrameUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for iframe handling.
 *
 * Scenario:
 * - Navigate to iframe page
 * - Interact with button inside iframe
 * - Capture screenshots
 */
public class TestFramesE2ETest {

    @Test
    public void iframeElementsCanBeAccessedAndInteractedWith() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_submit_get");

            // 2️⃣ Validate current URL using JS
            String currentUrl = page.evaluate("document.location.href").toString();
            assertThat(currentUrl).contains("tryjsref_submit_get");

            // 3️⃣ Click button inside iframe
            FrameUtil.clickInFrame(
                    page,
                    "[name='iframeResult']",
                    "body > button"
            );

            // 4️⃣ Screenshot element inside iframe
            FrameUtil.screenshotElementInFrame(
                    page,
                    "[name='iframeResult']",
                    "body > button",
                    Paths.get("target/screenshots/iframe_button.png")
            );

            // 5️⃣ Screenshot full page
            FrameUtil.takePageScreenshot(
                    page,
                    Paths.get("target/screenshots/full_page.png")
            );

            browser.close();
        }
    }
}
