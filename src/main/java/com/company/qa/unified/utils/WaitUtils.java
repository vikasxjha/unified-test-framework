package com.company.qa.unified.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * WaitUtils
 *
 * Centralized explicit wait utilities for:
 * - Selenium WebDriver
 * - Appium Driver (Android / iOS)
 *
 * RULES:
 * ❌ No Thread.sleep()
 * ❌ No implicit waits
 * ✅ Explicit waits only
 */
public final class WaitUtils {

    private static final int DEFAULT_POLLING_MS = 500;

    private WaitUtils() {
        // Utility class
    }

    /* =========================================================
       CORE WAIT FACTORY
       ========================================================= */

    private static WebDriverWait wait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLLING_MS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    /* =========================================================
       VISIBILITY
       ========================================================= */

    public static WebElement waitForVisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static List<WebElement> waitForAllVisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /* =========================================================
       CLICKABILITY
       ========================================================= */

    public static WebElement waitForClickable(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* =========================================================
       PRESENCE
       ========================================================= */

    public static WebElement waitForPresent(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /* =========================================================
       INVISIBILITY / DISAPPEAR
       ========================================================= */

    public static boolean waitForInvisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /* =========================================================
       TEXT & ATTRIBUTE
       ========================================================= */

    public static boolean waitForText(
            WebDriver driver,
            By locator,
            String expectedText,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.textToBePresentInElementLocated(
                        locator, expectedText));
    }

    public static boolean waitForAttribute(
            WebDriver driver,
            By locator,
            String attribute,
            String value,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.attributeContains(
                        locator, attribute, value));
    }

    /* =========================================================
       URL & PAGE STATE (WEB)
       ========================================================= */

    public static boolean waitForUrlContains(
            WebDriver driver,
            String partialUrl,
            int seconds
    ) {
        return wait(driver, seconds)
                .until(ExpectedConditions.urlContains(partialUrl));
    }

    /* =========================================================
       SAFE UTILITIES
       ========================================================= */

    public static boolean isElementVisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            waitForVisible(driver, locator, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static boolean isElementClickable(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            waitForClickable(driver, locator, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
