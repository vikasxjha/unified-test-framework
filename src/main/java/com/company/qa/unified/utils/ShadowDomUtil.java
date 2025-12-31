package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Utility class for interacting with Shadow DOM elements in Playwright.
 *
 * Playwright natively supports shadow DOM via:
 * - >>> (piercing selector)
 * - Locator chaining
 */
public final class ShadowDomUtil {

    private ShadowDomUtil() {
        // Utility class
    }

    /**
     * Returns a locator for an element inside Shadow DOM
     * using Playwright's piercing selector.
     *
     * @param page Playwright page
     * @param shadowSelector Shadow DOM piercing selector
     */
    public static Locator locate(Page page, String shadowSelector) {
        return page.locator(shadowSelector);
    }

    /**
     * Types text into a Shadow DOM element.
     */
    public static void type(Page page, String shadowSelector, String text) {
        Locator element = locate(page, shadowSelector);
        element.waitFor();
        element.fill(text);
    }

    /**
     * Clicks an element inside Shadow DOM.
     */
    public static void click(Page page, String shadowSelector) {
        Locator element = locate(page, shadowSelector);
        element.waitFor();
        element.click();
    }

    /**
     * Reads text from a Shadow DOM element.
     */
    public static String getText(Page page, String shadowSelector) {
        Locator element = locate(page, shadowSelector);
        element.waitFor();
        return element.textContent();
    }
}
