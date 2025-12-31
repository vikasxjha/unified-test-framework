package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Utility class for interacting with slider controls.
 *
 * Responsibilities:
 * - Calculate slider handle center
 * - Move slider by pixel offset
 * - Encapsulate mouse interactions
 */
public final class SliderUtil {

    private SliderUtil() {
        // Utility class
    }

    /**
     * Moves slider handle horizontally by given offset.
     *
     * @param page Playwright page
     * @param sliderHandle Locator of slider handle
     * @param xOffset Horizontal offset (positive → right, negative → left)
     */
    public static void moveSlider(Page page, Locator sliderHandle, double xOffset) {

        sliderHandle.waitFor();

        var box = sliderHandle.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Unable to determine slider handle position");
        }

        double startX = box.x + box.width / 2;
        double startY = box.y + box.height / 2;

        page.mouse().move(startX, startY);
        page.mouse().down();
        page.mouse().move(startX + xOffset, startY);
        page.mouse().up();
    }

    /**
     * Reads slider left position (CSS-based).
     * Useful when slider value is not visible.
     */
    public static String getSliderPosition(Page page, Locator sliderHandle) {
        return sliderHandle.evaluate("el => window.getComputedStyle(el).left").toString();
    }
}
