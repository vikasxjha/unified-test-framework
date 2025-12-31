package com.company.qa.unified.utils;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

/**
 * Utility class for working with iFrames in Playwright.
 *
 * Responsibilities:
 * - Frame resolution
 * - Safe element interaction inside frames
 * - Screenshot support
 */
public final class FrameUtil {

    private FrameUtil() {
        // Utility class
    }

    /**
     * Returns FrameLocator using frame selector.
     */
    public static FrameLocator getFrame(Page page, String frameSelector) {
        return page.frameLocator(frameSelector);
    }

    /**
     * Clicks an element inside a frame.
     */
    public static void clickInFrame(
            Page page,
            String frameSelector,
            String elementSelector
    ) {
        FrameLocator frame = getFrame(page, frameSelector);
        frame.locator(elementSelector).waitFor();
        frame.locator(elementSelector).click();
    }

    /**
     * Takes screenshot of element inside frame.
     */
    public static void screenshotElementInFrame(
            Page page,
            String frameSelector,
            String elementSelector,
            Path path
    ) {
        FrameLocator frame = getFrame(page, frameSelector);
        Locator element = frame.locator(elementSelector);
        element.waitFor();
        element.screenshot(new Locator.ScreenshotOptions().setPath(path));
    }

    /**
     * Takes full page screenshot.
     */
    public static void takePageScreenshot(Page page, Path path) {
        page.screenshot(new Page.ScreenshotOptions().setPath(path));
    }
}
