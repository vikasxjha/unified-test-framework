package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.CheckboxUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for checkbox handling.
 *
 * Scenario:
 * - Navigate to checkbox demo page
 * - Select all checkboxes
 * - Validate all selected
 * - Unselect all
 * - Validate all unselected
 */
public class HandlingCheckboxesE2ETest {

    @Test
    public void checkboxesCanBeSelectedAndUnselected() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("http://www.tizag.com/htmlT/htmlcheckboxes.php");
            assertThat(page.title()).contains("Checkbox");

            // 2️⃣ Locate checkbox container (relative & stable)
            Locator block = page.locator("div.example")
                    .first();

            Locator checkboxes = block.locator("input[type='checkbox']");

            int totalCheckboxes = checkboxes.count();
            assertThat(totalCheckboxes).isGreaterThan(0);

            // 3️⃣ Select all
            CheckboxUtil.selectAll(checkboxes);

            // 4️⃣ Validate selection
            assertThat(CheckboxUtil.countChecked(checkboxes))
                    .as("All checkboxes should be checked")
                    .isEqualTo(totalCheckboxes);

            // 5️⃣ Unselect all
            CheckboxUtil.unselectAll(checkboxes);

            // 6️⃣ Validate unselection
            assertThat(CheckboxUtil.countChecked(checkboxes))
                    .as("All checkboxes should be unchecked")
                    .isEqualTo(0);

            browser.close();
        }
    }
}
