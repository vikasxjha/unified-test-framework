package com.company.qa.unified.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Selenium wait utilities for web and mobile.
 *
 * Usage:
 *   WebElement element = WaitUtils.waitForVisible(driver, By.id("btn"), 10);
 *   WaitUtils.waitForClickable(driver, By.id("btn"), 10);
 */
public final class WaitUtils {

    private static final Log log = Log.get(WaitUtils.class);

    private WaitUtils() {
        // utility
    }

    /**
     * Wait for element to be visible.
     */
    public static WebElement waitForVisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(seconds)
            );
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(locator)
            );
        } catch (Exception e) {
            log.warn("Element not visible within {} seconds: {}", seconds, locator);
            throw new RuntimeException(
                    "Element not visible: " + locator, e);
        }
    }

    /**
     * Wait for element to be clickable.
     */
    public static WebElement waitForClickable(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(seconds)
            );
            return wait.until(
                    ExpectedConditions.elementToBeClickable(locator)
            );
        } catch (Exception e) {
            log.warn("Element not clickable within {} seconds: {}", seconds, locator);
            throw new RuntimeException(
                    "Element not clickable: " + locator, e);
        }
    }

    /**
     * Wait for element to be present.
     */
    public static WebElement waitForPresent(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(seconds)
            );
            return wait.until(
                    ExpectedConditions.presenceOfElementLocated(locator)
            );
        } catch (Exception e) {
            log.warn("Element not present within {} seconds: {}", seconds, locator);
            throw new RuntimeException(
                    "Element not present: " + locator, e);
        }
    }

    /**
     * Wait for element to disappear.
     */
    public static void waitForInvisible(
            WebDriver driver,
            By locator,
            int seconds
    ) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(seconds)
            );
            wait.until(
                    ExpectedConditions.invisibilityOfElementLocated(locator)
            );
            log.debug("Element became invisible: {}", locator);
        } catch (Exception e) {
            log.warn("Element still visible after {} seconds: {}", seconds, locator);
            throw new RuntimeException(
                    "Element did not become invisible: " + locator, e);
        }
    }

    /**
     * Wait for element text to change.
     */
    public static void waitForTextChange(
            WebDriver driver,
            By locator,
            String oldText,
            int seconds
    ) {
        try {
            WebDriverWait wait = new WebDriverWait(
                    driver,
                    Duration.ofSeconds(seconds)
            );
            wait.until(driver2 -> {
                WebElement element = driver2.findElement(locator);
                return !element.getText().equals(oldText);
            });
        } catch (Exception e) {
            log.warn("Text did not change within {} seconds", seconds);
            throw new RuntimeException(
                    "Text did not change: " + locator, e);
        }
    }

    /**
     * Wait for custom condition.
     */
    public static <T> T waitForCondition(
            WebDriver driver,
            com.google.common.base.Function<? super WebDriver, T> condition,
            int seconds
    ) {
        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(seconds)
        );
        return wait.until(condition);
    }
}

