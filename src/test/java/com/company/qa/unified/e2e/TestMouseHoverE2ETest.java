package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.MouseHoverUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for mouse hover functionality.
 *
 * Scenario:
 * - Navigate to application
 * - Hover over menu
 * - Click submenu item
 * - Validate navigation
 */
public class TestMouseHoverE2ETest {

    @Test
    public void menuCanBeHoveredAndSubmenuClicked() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://www.way2automation.com/");
            assertThat(page.title()).contains("Way2Automation");

            // 2️⃣ Hover over menu (Resources / Courses menu)
            MouseHoverUtil.hoverAndClick(
                    page,
                    "#menu-item-27580 > a",   // hover element
                    "#menu-item-27592 > a"    // submenu click
            );

            // 3️⃣ Validate navigation
            assertThat(page.url())
                    .as("Validate submenu navigation")
                    .contains("selenium");

            browser.close();
        }
    }
}
