package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Utility class for mouse hover interactions.
 *
 * Responsibilities:
 * - Hover over elements safely
 * - Click submenu items revealed after hover
 */
public final class MouseHoverUtil {

    private MouseHoverUtil() {
        // Utility class
    }

    /**
     * Hovers over an element.
     */
    public static void hover(Page page, String selector) {
        Locator element = page.locator(selector);
        element.waitFor();
        element.hover();
    }

    /**
     * Hovers over a parent element and clicks a child element.
     */
    public static void hoverAndClick(
            Page page,
            String hoverSelector,
            String clickSelector
    ) {
        Locator hoverElement = page.locator(hoverSelector);
        hoverElement.waitFor();
        hoverElement.hover();

        Locator clickElement = page.locator(clickSelector);
        clickElement.waitFor();
        clickElement.click();
    }
}
