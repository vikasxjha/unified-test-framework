package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Utility class for performing drag-and-drop actions.
 *
 * Responsibilities:
 * - Safely compute element centers
 * - Perform mouse-based drag and drop
 * - Keep interaction logic out of tests
 */
public final class DragAndDropUtil {

    private DragAndDropUtil() {
        // Utility class
    }

    /**
     * Drags a source element and drops it onto a target element.
     *
     * @param page Playwright page
     * @param source Draggable locator
     * @param target Droppable locator
     */
    public static void dragAndDrop(Page page, Locator source, Locator target) {

        // Ensure elements are visible and attached
        source.waitFor();
        target.waitFor();

        var sourceBox = source.boundingBox();
        var targetBox = target.boundingBox();

        if (sourceBox == null || targetBox == null) {
            throw new IllegalStateException("Unable to determine bounding box for drag/drop elements");
        }

        double sourceX = sourceBox.x + sourceBox.width / 2;
        double sourceY = sourceBox.y + sourceBox.height / 2;

        double targetX = targetBox.x + targetBox.width / 2;
        double targetY = targetBox.y + targetBox.height / 2;

        page.mouse().move(sourceX, sourceY);
        page.mouse().down();
        page.mouse().move(targetX, targetY);
        page.mouse().up();
    }
}
