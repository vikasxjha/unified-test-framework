package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.EvaluateJsUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test demonstrating safe JavaScript evaluation usage.
 *
 * Scenario:
 * - Read browser properties
 * - Read DOM values
 * - Modify DOM dynamically
 * - Validate changes
 */
public class TestEvaluateJsE2ETest {

    @Test
    public void javascriptEvaluationWorksAsExpected() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://example.com");
            assertThat(page.title()).contains("Example");

            // 2️⃣ Fetch document title via JS
            String titleViaJs =
                    EvaluateJsUtil.evaluateAsString(
                            page,
                            "document.title"
                    );

            assertThat(titleViaJs).isEqualTo(page.title());

            // 3️⃣ Fetch URL using window object
            String currentUrl =
                    EvaluateJsUtil.getWindowProperty(page, "location.href")
                            .toString();

            assertThat(currentUrl).contains("example.com");

            // 4️⃣ Inject DOM element using JS
            EvaluateJsUtil.evaluateAsString(
                    page,
                    """
                    const div = document.createElement('div');
                    div.id = 'js-added';
                    div.innerText = 'Added by JavaScript';
                    document.body.appendChild(div);
                    """
            );

            // 5️⃣ Validate injected element via Playwright
            Locator injected = page.locator("#js-added");
            assertThat(injected.textContent())
                    .isEqualTo("Added by JavaScript");

            // 6️⃣ Validate element existence via JS
            Boolean exists =
                    EvaluateJsUtil.evaluateAsBoolean(
                            page,
                            "document.getElementById('js-added') !== null"
                    );

            assertThat(exists).isTrue();

            browser.close();
        }
    }
}
