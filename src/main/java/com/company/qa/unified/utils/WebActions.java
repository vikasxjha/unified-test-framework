package com.company.qa.unified.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.time.Duration;

/**
 * WebActions
 *
 * Centralized helper for all Playwright web interactions.
 *
 * Responsibilities:
 * - Safe element interactions
 * - Explicit waits (no sleeps)
 * - Logging
 * - Screenshot capture
 * - JS execution helpers
 *
 * RULE:
 * ❌ Tests must NOT use Playwright Page directly
 * ✅ Pages must delegate actions to WebActions
 */
public class WebActions {

    private final Page page;
    private final Log log = Log.get(WebActions.class);

    public WebActions(Page page) {
        this.page = page;
    }

    /* =========================================================
       NAVIGATION
       ========================================================= */

    public void navigateTo(String url) {
        log.info("🌐 Navigating to {}", url);
        page.navigate(url, new Page.NavigateOptions()
                .setTimeout(30_000));
    }

    public String getCurrentUrl() {
        return page.url();
    }

    /* =========================================================
       WAITS
       ========================================================= */

    public void waitForVisible(String selector, int seconds) {
        log.debug("⏳ Waiting for visible: {}", selector);
        page.waitForSelector(
                selector,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(seconds * 1000L)
        );
    }

    public void waitForHidden(String selector, int seconds) {
        log.debug("⏳ Waiting for hidden: {}", selector);
        page.waitForSelector(
                selector,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(seconds * 1000L)
        );
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    public void click(String selector) {
        waitForVisible(selector, 10);
        log.info("🖱 Clicking {}", selector);
        page.click(selector);
    }

    public void type(String selector, String text) {
        waitForVisible(selector, 10);
        log.info("⌨️ Typing into {} value=[REDACTED]", selector);
        page.fill(selector, text);
    }

    public void clearAndType(String selector, String text) {
        waitForVisible(selector, 10);
        page.fill(selector, "");
        page.fill(selector, text);
    }

    public String getText(String selector) {
        waitForVisible(selector, 10);
        String text = page.textContent(selector);
        log.debug("📄 Text from {} = {}", selector, text);
        return text;
    }

    public boolean isVisible(String selector) {
        return page.isVisible(selector);
    }

    public boolean isEnabled(String selector) {
        return page.isEnabled(selector);
    }

    /* =========================================================
       DROPDOWNS & CHECKBOXES
       ========================================================= */

    public void selectByValue(String selector, String value) {
        log.info("🔽 Selecting value={} from {}", value, selector);
        page.selectOption(selector, value);
    }

    public void check(String selector) {
        log.info("☑️ Checking {}", selector);
        if (!page.isChecked(selector)) {
            page.check(selector);
        }
    }

    public void uncheck(String selector) {
        log.info("⬜ Unchecking {}", selector);
        if (page.isChecked(selector)) {
            page.uncheck(selector);
        }
    }

    /* =========================================================
       SCROLLING
       ========================================================= */

    public void scrollIntoView(String selector) {
        log.debug("⬇️ Scrolling into view: {}", selector);
        page.locator(selector).scrollIntoViewIfNeeded();
    }

    public void scrollToBottom() {
        log.debug("⬇️ Scrolling to bottom");
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public void assertTextEquals(String selector, String expected) {
        String actual = getText(selector);
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Text mismatch for " + selector +
                            " expected=[" + expected +
                            "] actual=[" + actual + "]"
            );
        }
    }

    public void assertVisible(String selector) {
        if (!isVisible(selector)) {
            throw new AssertionError("Element not visible: " + selector);
        }
    }

    /* =========================================================
       JAVASCRIPT
       ========================================================= */

    public Object executeJs(String script) {
        log.debug("⚙️ Executing JS");
        return page.evaluate(script);
    }

    /* =========================================================
       SCREENSHOTS
       ========================================================= */

    public byte[] takeScreenshot(String name) {
        log.info("📸 Taking screenshot: {}", name);
        byte[] screenshot = page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(Path.of("reports/screenshots/" + name + ".png"))
                        .setFullPage(true)
        );
        return screenshot;
    }

    /* =========================================================
       NETWORK & PAGE STATE
       ========================================================= */

    public void waitForNetworkIdle(int seconds) {
        log.debug("🌐 Waiting for network idle");
        page.waitForLoadState(
                LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions()
                        .setTimeout(seconds * 1000L)
        );
    }

    public void waitForDomReady() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    /* =========================================================
       CLEANUP
       ========================================================= */

    public void close() {
        log.info("❌ Closing page");
        page.close();
    }
}
