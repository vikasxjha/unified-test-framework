package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.ResizableUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for resizable UI components.
 *
 * Scenario:
 * - Navigate to resizable demo page
 * - Resize element using resize handle
 * - Validate element size change
 */
public class TestResizableE2ETest {

    @Test
    public void elementCanBeResizedSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://jqueryui.com/resources/demos/resizable/default.html");

            Locator resizableBox = page.locator("#resizable");
            Locator resizeHandle = page.locator("#resizable .ui-resizable-se");

            // Capture initial size
            double initialWidth = resizableBox.boundingBox().width;
            double initialHeight = resizableBox.boundingBox().height;

            // 2️⃣ Resize element
            ResizableUtil.resize(page, resizeHandle, 150, 150);

            // Capture resized dimensions
            double newWidth = resizableBox.boundingBox().width;
            double newHeight = resizableBox.boundingBox().height;

            // 3️⃣ Validate resize occurred
            assertThat(newWidth).isGreaterThan(initialWidth);
            assertThat(newHeight).isGreaterThan(initialHeight);

            browser.close();
        }
    }
}
