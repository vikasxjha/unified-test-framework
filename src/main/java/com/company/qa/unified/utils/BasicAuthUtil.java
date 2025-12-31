package com.company.qa.unified.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;

/**
 * Utility for creating Playwright browser contexts
 * with HTTP Basic Authentication configured.
 *
 * Responsibility:
 * - Centralize Basic Auth handling
 * - Keep credentials out of tests
 */
public final class BasicAuthUtil {

    private BasicAuthUtil() {
        // Utility class
    }

    /**
     * Creates a BrowserContext with HTTP Basic Auth enabled.
     *
     * @param browser Playwright browser instance
     * @param username Basic auth username
     * @param password Basic auth password
     * @return authenticated BrowserContext
     */
    public static BrowserContext createAuthenticatedContext(
            Browser browser,
            String username,
            String password
    ) {

        return browser.newContext(
                new Browser.NewContextOptions()
                        .setHttpCredentials(username, password)
                        .setViewportSize(1280, 720)
        );
    }
}
