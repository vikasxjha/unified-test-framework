package com.company.qa.unified.utils;

import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility to create Playwright browser contexts with video recording enabled.
 *
 * Designed for:
 * - E2E debugging
 * - CI artifact capture
 * - Failure analysis
 */
public final class VideoCaptureUtil {

    private VideoCaptureUtil() {
        // Utility class
    }

    public static BrowserContext createContextWithVideo(
            Playwright playwright,
            BrowserType browserType,
            boolean headless,
            String videoDir
    ) {
        Browser browser = browserType.launch(
                new BrowserType.LaunchOptions().setHeadless(headless)
        );

        Path videoPath = Paths.get(videoDir);

        return browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(videoPath)
                        .setViewportSize(1280, 720)
        );
    }
}
