package com.company.qa.unified.drivers;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.utils.Log;
import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

/**
 * Central factory for Playwright (Java).
 *
 * Supports:
 * - Chromium / Firefox / WebKit
 * - Headless / Headed
 * - Tracing, video, screenshots
 * - Parallel execution via ThreadLocal
 *
 * RULE:
 * ❌ Tests must NOT create Playwright/Page directly
 * ✅ Tests must ALWAYS use PlaywrightDriverFactory
 */
public final class PlaywrightDriverFactory {

    private static final Log log =
            Log.get(PlaywrightDriverFactory.class);

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private static final ThreadLocal<Playwright> PLAYWRIGHT =
            new ThreadLocal<>();

    private static final ThreadLocal<Browser> BROWSER =
            new ThreadLocal<>();

    private static final ThreadLocal<BrowserContext> CONTEXT =
            new ThreadLocal<>();

    private static final ThreadLocal<Page> PAGE =
            new ThreadLocal<>();

    private PlaywrightDriverFactory() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException(
                    "Playwright Page not initialized. Call init() first.");
        }
        return page;
    }

    public static BrowserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * Initialize Playwright + Browser + Context + Page
     */
    public static void init() {

        if (PAGE.get() != null) {
            return;
        }

        log.info("🌐 Initializing Playwright");

        Playwright playwright = Playwright.create();
        Browser browser = launchBrowser(playwright);
        BrowserContext context = createContext(browser);
        Page page = context.newPage();

        // Default timeouts
        page.setDefaultTimeout(
                Duration.ofSeconds(
                        RuntimeConfig.uiTimeoutSeconds()
                ).toMillis()
        );

        PAGE.set(page);
        CONTEXT.set(context);
        BROWSER.set(browser);
        PLAYWRIGHT.set(playwright);

        // Navigate to base URL if configured
        if (ENV.getWebBaseUrl() != null) {
            page.navigate(ENV.getWebBaseUrl());
        }
    }

    /**
     * Cleanup everything for the current thread.
     */
    public static void teardown() {

        log.info("🧹 Tearing down Playwright");

        try {
            if (CONTEXT.get() != null) {
                stopTracing();
                CONTEXT.get().close();
            }
        } catch (Exception ignored) {}

        try {
            if (BROWSER.get() != null) {
                BROWSER.get().close();
            }
        } catch (Exception ignored) {}

        try {
            if (PLAYWRIGHT.get() != null) {
                PLAYWRIGHT.get().close();
            }
        } catch (Exception ignored) {}

        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
    }

    /* =========================================================
       BROWSER LAUNCH
       ========================================================= */

    private static Browser launchBrowser(Playwright playwright) {

        String browserName =
                System.getProperty("browser", "chromium")
                        .toLowerCase(Locale.ROOT);

        boolean headless =
                RuntimeConfig.headless();

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(
                                RuntimeConfig.slowMoMillis()
                        );

        log.info("Launching browser={} headless={}",
                browserName, headless);

        return switch (browserName) {
            case "firefox" ->
                    playwright.firefox().launch(options);
            case "webkit" ->
                    playwright.webkit().launch(options);
            default ->
                    playwright.chromium().launch(options);
        };
    }

    /* =========================================================
       CONTEXT CONFIGURATION
       ========================================================= */

    private static BrowserContext createContext(Browser browser) {

        Browser.NewContextOptions options =
                new Browser.NewContextOptions()
                        .setViewportSize(1280, 800)
                        .setBaseURL(ENV.getWebBaseUrl())
                        .setIgnoreHTTPSErrors(true);

        if (RuntimeConfig.recordVideo()) {
            options.setRecordVideoDir(
                    Path.of("reports/videos"));
        }

        BrowserContext context = browser.newContext(options);

        startTracing(context);
        return context;
    }

    /* =========================================================
       TRACING
       ========================================================= */

    private static void startTracing(BrowserContext context) {
        if (!RuntimeConfig.enableTracing()) {
            return;
        }

        log.info("🎥 Starting Playwright tracing");

        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );
    }

    private static void stopTracing() {
        if (!RuntimeConfig.enableTracing()) {
            return;
        }

        try {
            Path tracePath =
                    Path.of("reports/traces",
                            "trace-" + System.currentTimeMillis() + ".zip");

            CONTEXT.get().tracing().stop(
                    new Tracing.StopOptions()
                            .setPath(tracePath)
            );

            log.info("🎥 Trace saved: {}", tracePath);

        } catch (Exception e) {
            log.warn("Failed to stop tracing", e);
        }
    }
}
