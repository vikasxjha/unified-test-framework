package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for handling browser alerts, confirms, and prompts in Playwright.
 *
 * Responsibilities:
 * - Capture dialog messages
 * - Accept or dismiss dialogs
 * - Provide message back to tests
 */
public final class AlertUtil {

    private AlertUtil() {
        // Utility class
    }

    /**
     * Registers a dialog handler that automatically accepts alerts
     * and captures the dialog message.
     *
     * @param page Playwright page
     * @return reference holding dialog message
     */
    public static AtomicReference<String> acceptAlert(Page page) {

        AtomicReference<String> dialogMessage = new AtomicReference<>();

        page.onceDialog(dialog -> {
            dialogMessage.set(dialog.message());
            dialog.accept();
        });

        return dialogMessage;
    }

    /**
     * Registers a dialog handler that dismisses alerts.
     */
    public static AtomicReference<String> dismissAlert(Page page) {

        AtomicReference<String> dialogMessage = new AtomicReference<>();

        page.onceDialog(dialog -> {
            dialogMessage.set(dialog.message());
            dialog.dismiss();
        });

        return dialogMessage;
    }

    /**
     * Handles prompt dialog by entering text and accepting.
     */
    public static AtomicReference<String> handlePrompt(
            Page page,
            String inputText
    ) {
        AtomicReference<String> dialogMessage = new AtomicReference<>();

        page.onceDialog(dialog -> {
            dialogMessage.set(dialog.message());
            dialog.accept(inputText);
        });

        return dialogMessage;
    }
}
