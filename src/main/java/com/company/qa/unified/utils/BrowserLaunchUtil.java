package com.company.qa.unified.utils;

import com.microsoft.playwright.*;

import java.awt.*;
import java.util.Objects;

/**
 * Utility class responsible for launching browsers and creating contexts.
 *
 * Responsibilities:
 * - Launch browser with channel / headless options
 * - Create browser context with full screen viewport
 * - Centralize Playwright lifecycle management
 */
public final class BrowserLaunchUtil {

    private BrowserLaunchUtil() {
        // utility class
    }

    /**
     * Launches a browser with given options.
     */
    public static Browser launchBrowser(
            Playwright playwright,
            String browserName,
            String channel,
            boolean headless
    ) {

        BrowserType browserType = switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions().setHeadless(headless);

        if (Objects.nonNull(channel) && !channel.isBlank()) {
            options.setChannel(channel);
        }

        return browserType.launch(options);
    }

    /**
     * Creates a browser context with full screen viewport.
     */
    public static BrowserContext createFullScreenContext(Browser browser) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        return browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(
                                (int) screenSize.getWidth(),
                                (int) screenSize.getHeight()
                        )
        );
    }
}
