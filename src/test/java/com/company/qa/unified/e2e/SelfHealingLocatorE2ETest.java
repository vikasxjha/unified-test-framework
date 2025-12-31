package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.AiSelfHealingLocatorUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test demonstrating AI-powered self-healing locators.
 */
public class SelfHealingLocatorE2ETest {

    @Test
    public void brokenLocatorIsHealedUsingAI() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));

            Page page = browser.newPage();
            page.navigate("https://www.wikipedia.org/");

            // ❌ Intentionally broken locator
            String brokenLocator = "#searchInput_WRONG";

            // 🧠 Human intent (VERY IMPORTANT)
            String intent = "Search input box on Wikipedia homepage";

            // AI Healing
            String healedLocator =
                    AiSelfHealingLocatorUtil.healLocatorIfNeeded(
                            page,
                            brokenLocator,
                            intent
                    );

            // Retry with healed locator
            page.locator(healedLocator).fill("Playwright");

            assertThat(page.locator(healedLocator).inputValue())
                    .isEqualTo("Playwright");

            browser.close();
        }
    }
}
