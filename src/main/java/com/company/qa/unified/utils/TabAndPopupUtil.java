package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;

import java.util.function.Consumer;

/**
 * Utility for handling browser tabs and popups in Playwright.
 *
 * Responsibilities:
 * - Wait for new tab / popup
 * - Execute actions on the popup safely
 * - Keep popup-handling logic out of tests
 */
public final class TabAndPopupUtil {

    private TabAndPopupUtil() {
        // Utility class
    }

    /**
     * Opens a popup/tab and returns the new Page.
     *
     * @param parentPage Page that triggers the popup
     * @param triggerAction Action that opens the popup
     * @return Popup Page
     */
    public static Page openPopup(Page parentPage, Runnable triggerAction) {
        return parentPage.waitForPopup(triggerAction);
    }

    /**
     * Executes actions inside a popup and closes it.
     */
    public static void handlePopup(Page popup, Consumer<Page> popupActions) {
        popupActions.accept(popup);
        popup.close();
    }
}
