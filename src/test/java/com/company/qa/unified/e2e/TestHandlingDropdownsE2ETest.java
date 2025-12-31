package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.DropdownUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for handling dropdowns.
 *
 * Scenario:
 * - Navigate to Wikipedia
 * - Select dropdown options by value, label, index
 * - Validate available dropdown values
 */
public class TestHandlingDropdownsE2ETest {

    @Test
    public void dropdownSelectionsWorkCorrectly() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.wikipedia.org/");
            assertThat(page.title()).contains("Wikipedia");

            String dropdownSelector = "select";

            // 2️⃣ Select by value
            DropdownUtil.selectByValue(page, dropdownSelector, "hi");

            // 3️⃣ Select by label
            DropdownUtil.selectByLabel(page, dropdownSelector, "Eesti");

            // 4️⃣ Select by index
            DropdownUtil.selectByIndex(page, dropdownSelector, 1);

            // 5️⃣ Validate dropdown options
            int optionCount = DropdownUtil.getOptionCount(page, dropdownSelector);
            assertThat(optionCount).isGreaterThan(5);

            List<String> optionTexts =
                    DropdownUtil.getAllOptionTexts(page, dropdownSelector);

            assertThat(optionTexts)
                    .contains("English", "Deutsch", "Español");

            browser.close();
        }
    }
}
