package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.SliderUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for slider interaction.
 *
 * Scenario:
 * - Navigate to slider demo page
 * - Move slider handle
 * - Validate slider position changes
 */
public class TestSlidersE2ETest {

    @Test
    public void sliderCanBeMovedSuccessfully() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            Page page = browser.newPage();

            // 1️⃣ Navigate
            page.navigate("https://jqueryui.com/resources/demos/slider/default.html");

            Locator sliderHandle = page.locator("#slider > span");

            // Capture initial slider position
            String initialPosition =
                    SliderUtil.getSliderPosition(page, sliderHandle);

            // 2️⃣ Move slider to the right
            SliderUtil.moveSlider(page, sliderHandle, 200);

            // Capture updated position
            String newPosition =
                    SliderUtil.getSliderPosition(page, sliderHandle);

            // 3️⃣ Validate slider moved
            assertThat(newPosition)
                    .as("Slider position should change after move")
                    .isNotEqualTo(initialPosition);

            browser.close();
        }
    }
}
