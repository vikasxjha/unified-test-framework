package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.TabAndPopupUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for handling browser tabs and popups.
 *
 * Scenario:
 * - Navigate to signup page
 * - Open Privacy Policy in a new tab
 * - Interact with popup
 * - Close popup safely
 */
public class HandlingTabsAndPopupsE2ETest {

    @Test
    public void popupTabCanBeHandledSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://sso.teachable.com/secure/673/identity/sign_up/email");
            assertThat(page.title()).contains("Sign Up");

            // 2️⃣ Open popup (new tab)
            Page popup = TabAndPopupUtil.openPopup(
                    page,
                    () -> page.locator("text=Privacy Policy").first().click()
            );

            // 3️⃣ Validate popup opened
            assertThat(popup.url()).contains("privacy");

            // 4️⃣ Interact with popup
            TabAndPopupUtil.handlePopup(popup, p -> {
                p.locator("#header-sign-up-btn").click();
                p.locator("#user_name").fill("Rahul Arora");

                String value = p.locator("#user_name").inputValue();
                assertThat(value).isEqualTo("Rahul Arora");
            });

            // 5️⃣ Validate parent page is still active
            assertThat(page.isClosed()).isFalse();

            browser.close();
        }
    }
}
