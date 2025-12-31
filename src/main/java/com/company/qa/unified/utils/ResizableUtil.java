package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Utility class for handling resizable UI components.
 *
 * Responsibilities:
 * - Calculate resize handle position
 * - Perform resize using mouse interactions
 * - Encapsulate resize logic
 */
public final class ResizableUtil {

    private ResizableUtil() {
        // Utility class
    }

    /**
     * Resizes an element using its resize handle by offset.
     *
     * @param page Playwright page
     * @param resizeHandle Locator for resize handle
     * @param xOffset Horizontal offset
     * @param yOffset Vertical offset
     */
    public static void resize(
            Page page,
            Locator resizeHandle,
            double xOffset,
            double yOffset
    ) {

        resizeHandle.waitFor();

        var box = resizeHandle.boundingBox();
        if (box == null) {
            throw new IllegalStateException("Unable to determine bounding box for resize handle");
        }

        double startX = box.x + box.width / 2;
        double startY = box.y + box.height / 2;

        page.mouse().move(startX, startY);
        page.mouse().down();
        page.mouse().move(startX + xOffset, startY + yOffset);
        page.mouse().up();
    }
}
