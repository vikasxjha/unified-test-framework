package com.company.qa.unified.utils;

import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Utility class for handling keyboard interactions in Playwright.
 *
 * Responsibilities:
 * - Typing with delay
 * - Common keyboard shortcuts
 * - Navigation keys
 */
public final class KeyboardUtil {

    private KeyboardUtil() {
        // Utility class
    }

    /**
     * Types text into a locator with per-character delay.
     */
    public static void typeWithDelay(Page page, String selector, String text, int delayMs) {
        page.locator(selector).waitFor(
                new com.microsoft.playwright.Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
        );
        page.locator(selector).type(text,
                new com.microsoft.playwright.Locator.TypeOptions().setDelay(delayMs));
    }

    /**
     * Presses a single key (Enter, Escape, etc.)
     */
    public static void press(Page page, String key) {
        page.keyboard().press(key);
    }

    /**
     * Performs Ctrl + key shortcut.
     */
    public static void pressCtrl(Page page, String key) {
        page.keyboard().press("Control+" + key);
    }

    /**
     * Presses arrow key multiple times.
     */
    public static void pressArrow(Page page, String direction, int times) {
        Keyboard keyboard = page.keyboard();
        for (int i = 0; i < times; i++) {
            keyboard.press(direction);
        }
    }

    /**
     * Holds a modifier key while pressing another key.
     */
    public static void pressWithModifier(Page page, String modifier, String key) {
        Keyboard keyboard = page.keyboard();
        keyboard.down(modifier);
        keyboard.press(key);
        keyboard.up(modifier);
    }
}
