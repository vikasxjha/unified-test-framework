package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;


/**
 * Utility class for executing JavaScript in browser context safely.
 *
 * IMPORTANT:
 * - Use evaluate ONLY when Playwright APIs cannot solve the problem
 * - Never use it for normal clicks, typing, or waits
 */
public final class EvaluateJsUtil {

    private EvaluateJsUtil() {
        // Utility class
    }

    /**
     * Executes JavaScript and returns result as String.
     */
    public static String evaluateAsString(Page page, String script) {
        Object result = page.evaluate(script);
        return result != null ? result.toString() : null;
    }

    /**
     * Executes JavaScript and returns Boolean.
     */
    public static Boolean evaluateAsBoolean(Page page, String script) {
        return (Boolean) page.evaluate(script);
    }

    /**
     * Executes JavaScript with arguments.
     */
    public static Object evaluateWithArgs(Page page, String script, Object arg) {
        return page.evaluate(script, arg);
    }

    /**
     * Fetches window-level properties safely.
     */
    public static Object getWindowProperty(Page page, String property) {
        return page.evaluate("prop => window[prop]", property);
    }

    /**
     * Fetches document-level properties.
     */
    public static Object getDocumentProperty(Page page, String property) {
        return page.evaluate("prop => document[prop]", property);
    }
}
