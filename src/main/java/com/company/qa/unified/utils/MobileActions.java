package com.company.qa.unified.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.List;

/**
 * MobileActions
 *
 * Centralized helper for all mobile interactions.
 *
 * Supports:
 * - Android & iOS
 * - Safe waits
 * - Retry-aware taps
 * - Gestures
 * - Keyboard handling
 * - Screenshot capture
 *
 * RULE:
 * ❌ Screens & tests should NOT duplicate low-level logic
 * ✅ Always delegate to MobileActions
 */
public class MobileActions {

    private static final Log log =
            Log.get(MobileActions.class);

    private final AppiumDriver<?> driver;

    public MobileActions(AppiumDriver<?> driver) {
        this.driver = driver;
    }

    /* =========================================================
       BASIC ACTIONS
       ========================================================= */

    public void tap(By locator) {
        log.info("📱 Tapping {}", locator);
        WaitUtils.waitForClickable(driver, locator, 10).click();
    }

    public void type(By locator, String text) {
        log.info("⌨️ Typing into {} value=[REDACTED]", locator);
        WebElement el =
                WaitUtils.waitForVisible(driver, locator, 10);
        el.clear();
        el.sendKeys(text);
        hideKeyboard();
    }

    public String getText(By locator) {
        return WaitUtils.waitForVisible(driver, locator, 10).getText();
    }

    public boolean isVisible(By locator, int seconds) {
        return WaitUtils.isElementVisible(driver, locator, seconds);
    }

    /* =========================================================
       RETRY-SAFE ACTIONS
       ========================================================= */

    public void tapWithRetry(By locator, int attempts) {
        for (int i = 1; i <= attempts; i++) {
            try {
                tap(locator);
                return;
            } catch (Exception e) {
                log.warn("Retry {}/{} tapping {}", i, attempts, locator);
                if (i == attempts) {
                    throw e;
                }
            }
        }
    }

    /* =========================================================
       GESTURES
       ========================================================= */

    public void swipeVertical(double startRatio, double endRatio) {

        Dimension size = driver.manage().window().getSize();
        int x = size.width / 2;
        int startY = (int) (size.height * startRatio);
        int endY = (int) (size.height * endRatio);

        log.debug("📲 Swipe vertical {} -> {}", startY, endY);

        new TouchAction<>(driver)
                .press(PointOption.point(x, startY))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(600)))
                .moveTo(PointOption.point(x, endY))
                .release()
                .perform();
    }

    public void swipeUp() {
        swipeVertical(0.8, 0.2);
    }

    public void swipeDown() {
        swipeVertical(0.2, 0.8);
    }

    public void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        int y = size.height / 2;
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);

        new TouchAction<>(driver)
                .press(PointOption.point(startX, y))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(600)))
                .moveTo(PointOption.point(endX, y))
                .release()
                .perform();
    }

    /* =========================================================
       SCROLLING
       ========================================================= */

    public void scrollToElement(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            List<WebElement> elements =
                    driver.findElements(locator);
            if (!elements.isEmpty()) {
                return;
            }
            swipeUp();
        }
        throw new NoSuchElementException(
                "Element not found after scrolling: " + locator
        );
    }

    /* =========================================================
       KEYBOARD & NAVIGATION
       ========================================================= */

    public void hideKeyboard() {
        try {
            driver.hideKeyboard();
        } catch (Exception ignored) {
        }
    }

    public void pressBack() {
        driver.navigate().back();
    }

    /* =========================================================
       SCREENSHOT
       ========================================================= */

    public byte[] takeScreenshot(String name) {

        log.info("📸 Capturing mobile screenshot: {}", name);

        byte[] image =
                ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);

        ReportPublisher.attachScreenshot(name, image);
        return image;
    }

    /* =========================================================
       WAIT HELPERS
       ========================================================= */

    public void waitForVisible(By locator, int seconds) {
        WaitUtils.waitForVisible(driver, locator, seconds);
    }

    public void waitForInvisible(By locator, int seconds) {
        WaitUtils.waitForInvisible(driver, locator, seconds);
    }
}
