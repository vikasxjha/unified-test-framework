package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.KeyboardUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for keyboard interactions.
 *
 * Scenario:
 * - Type text with delay
 * - Use Enter key
 * - Use copy / cut / paste shortcuts
 * - Navigate cursor using arrow keys
 */
public class HandlingKeyboardEventsE2ETest {

    @Test
    public void keyboardInteractionsWorkCorrectly() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://login.yahoo.com/");
            assertThat(page.title()).contains("Yahoo");

            // 2️⃣ Type username with delay
            KeyboardUtil.typeWithDelay(
                    page,
                    "#login-username",
                    "trainer@way2automation",
                    100
            );

            // 3️⃣ Press Enter
            KeyboardUtil.press(page, "Enter");

            // 4️⃣ Navigate back
            page.goBack();

            // 5️⃣ Select all (Ctrl+A) and cut (Ctrl+X)
            KeyboardUtil.pressCtrl(page, "A");
            KeyboardUtil.pressCtrl(page, "X");

            // 6️⃣ Paste back (Ctrl+V)
            KeyboardUtil.pressCtrl(page, "V");

            // 7️⃣ Move cursor left using arrow keys
            KeyboardUtil.pressArrow(page, "ArrowLeft", 3);

            // 8️⃣ Validate input still contains expected text
            String value = page.locator("#login-username").inputValue();
            assertThat(value).contains("trainer@way2automation");

            browser.close();
        }
    }
}
