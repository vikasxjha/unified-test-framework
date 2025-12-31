package com.company.qa.unified.utils;

import com.microsoft.playwright.Locator;

/**
 * Utility class for handling checkbox interactions.
 *
 * Responsibilities:
 * - Select / unselect checkboxes safely
 * - Query checkbox state
 * - Perform bulk operations
 */
public final class CheckboxUtil {

    private CheckboxUtil() {
        // Utility class
    }

    /**
     * Selects all unchecked checkboxes inside the given locator.
     */
    public static void selectAll(Locator checkboxes) {
        int count = checkboxes.count();
        for (int i = 0; i < count; i++) {
            Locator checkbox = checkboxes.nth(i);
            if (!checkbox.isChecked()) {
                checkbox.check();
            }
        }
    }

    /**
     * Unselects all checked checkboxes inside the given locator.
     */
    public static void unselectAll(Locator checkboxes) {
        int count = checkboxes.count();
        for (int i = 0; i < count; i++) {
            Locator checkbox = checkboxes.nth(i);
            if (checkbox.isChecked()) {
                checkbox.uncheck();
            }
        }
    }

    /**
     * Returns the number of checked checkboxes.
     */
    public static int countChecked(Locator checkboxes) {
        int checked = 0;
        int count = checkboxes.count();
        for (int i = 0; i < count; i++) {
            if (checkboxes.nth(i).isChecked()) {
                checked++;
            }
        }
        return checked;
    }
}
