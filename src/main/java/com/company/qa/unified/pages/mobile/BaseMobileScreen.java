package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.*;

import java.util.Map;

/**
 * BaseMobileScreen
 *
 * Base class for all Android & iOS screen objects.
 *
 * Responsibilities:
 * - Hold AppiumDriver reference
 * - Provide common waits
 * - Provide basic gestures
 * - Provide safe element checks
 * - Screenshot utilities
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Screens must EXTEND BaseMobileScreen
 */
public abstract class BaseMobileScreen {

    protected final AppiumDriver driver;
    protected final Log log;

    protected BaseMobileScreen(AppiumDriver driver) {
        this.driver = driver;
        this.log = Log.get(this.getClass());
    }

    /* =========================================================
       COMMON WAITS
       ========================================================= */

    protected WebElement waitForVisible(By locator, int seconds) {
        return WaitUtils.waitForVisible(driver, locator, seconds);
    }

    protected WebElement waitForClickable(By locator, int seconds) {
        return WaitUtils.waitForClickable(driver, locator, seconds);
    }

    protected boolean isElementPresent(By locator, int seconds) {
        try {
            WaitUtils.waitForVisible(driver, locator, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /* =========================================================
       BASIC ACTIONS
       ========================================================= */

    protected void tap(By locator) {
        waitForClickable(locator, 10).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator, 10);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisible(locator, 10).getText();
    }

    /* =========================================================
       GESTURES
       ========================================================= */

    /**
     * Swipe vertically.
     */
    protected void swipeVertical(double startRatio, double endRatio) {

        Dimension size = driver.manage().window().getSize();

        int startY = (int) (size.height * startRatio);
        int endY = (int) (size.height * endRatio);

        log.debug("📲 Swipe vertical from {} to {}", startY, endY);

        // Modern Appium 9.1.0+ gesture API using mobile: swipe
        try {
            driver.executeScript("mobile: swipe", Map.of(
                    "direction", startY > endY ? "up" : "down",
                    "percent", Math.abs(endRatio - startRatio)
            ));
        } catch (Exception e) {
            log.warn("Swipe gesture failed: {}", e.getMessage());
        }
    }

    /**
     * Swipe up.
     */
    protected void swipeUp() {
        swipeVertical(0.8, 0.2);
    }

    /**
     * Swipe down.
     */
    protected void swipeDown() {
        swipeVertical(0.2, 0.8);
    }

    /**
     * Tap on screen coordinates.
     */
    protected void tapByCoordinates(int x, int y) {

        log.debug("📍 Tapping at coordinates x={} y={}", x, y);

        try {
            driver.executeScript("mobile: tap", Map.of(
                    "x", x,
                    "y", y
            ));
        } catch (Exception e) {
            log.warn("Tap gesture failed: {}", e.getMessage());
        }
    }

    /* =========================================================
       SCROLL UTILITIES
       ========================================================= */

    /**
     * Scroll until element is visible (Android only).
     */
    protected void scrollToElement(By locator) {
        for (int i = 0; i < 5; i++) {
            if (driver.findElements(locator).size() > 0) {
                return;
            }
            swipeUp();
        }
        throw new NoSuchElementException(
                "Element not found after scrolling: " + locator
        );
    }

    /* =========================================================
       KEYBOARD & BACK
       ========================================================= */

    protected void hideKeyboard() {
        try {
            // Modern Appium 9.1.0+ API
            // Use executeScript to dismiss keyboard on iOS/Android
            driver.executeScript("mobile: dismissKeyboard");
        } catch (Exception ignored) {
            // Keyboard may not be open or not supported
        }
    }

    protected void pressBack() {
        driver.navigate().back();
    }

    /* =========================================================
       SCREENSHOT
       ========================================================= */

    /**
     * Capture screenshot for reporting/debugging.
     */
    protected byte[] takeScreenshot() {

        log.debug("📸 Taking screenshot");

        return ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);
    }
}
