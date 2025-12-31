package com.company.qa.unified.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;
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

    private final AppiumDriver driver;

    public MobileActions(AppiumDriver driver) {
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

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), x, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
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

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, y));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
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
            // In Appium 9.x, hideKeyboard is removed. Use driver.executeScript instead
            driver.executeScript("mobile: hideKeyboard");
        } catch (Exception e) {
            // Try alternate method - press back button on Android or tap outside on iOS
            try {
                driver.navigate().back();
            } catch (Exception ignored) {
                // Keyboard hiding not supported or already hidden
            }
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
