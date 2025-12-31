package com.company.qa.unified.utils;

import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility for launching NON-INCOGNITO (persistent) browser contexts.
 *
 * Persistent context:
 * - Preserves cookies
 * - Preserves localStorage
 * - Preserves cache
 * - Mimics real user browser profile
 */
public final class PersistentBrowserUtil {

    private PersistentBrowserUtil() {
        // utility class
    }

    /**
     * Launches a persistent (non-incognito) browser context.
     *
     * @param playwright Playwright instance
     * @param userDataDir Directory for browser profile
     * @param executablePath Optional browser executable path
     * @param headless Headless mode
     * @return BrowserContext
     */
    public static BrowserContext launchPersistentContext(
            Playwright playwright,
            String userDataDir,
            String executablePath,
            boolean headless
    ) {

        BrowserType.LaunchPersistentContextOptions options =
                new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(headless);

        if (executablePath != null && !executablePath.isBlank()) {
            options.setExecutablePath(Paths.get(executablePath));
        }

        return playwright.chromium()
                .launchPersistentContext(
                        Paths.get(userDataDir),
                        options
                );
    }
}
