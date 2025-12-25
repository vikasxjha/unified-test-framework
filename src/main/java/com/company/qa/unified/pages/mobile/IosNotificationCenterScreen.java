package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IosNotificationCenterScreen
 *
 * Represents the in-app Notification Center on iOS.
 *
 * Covers:
 * - Viewing notifications
 * - Read / unread state
 * - Mark all as read
 * - Open notification
 * - Navigation back to Home
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class IosNotificationCenterScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(IosNotificationCenterScreen.class);

    private final IOSDriver driver;

    /* =========================================================
       LOCATORS (XCUI)
       ========================================================= */

    private final By notificationCenterRoot =
            AppiumBy.accessibilityId("notification_center_root");

    private final By notificationCell =
            AppiumBy.iOSClassChain("**/XCUIElementTypeCell");

    private final By notificationTitle =
            AppiumBy.iOSClassChain(
                    "**/XCUIElementTypeCell/**/XCUIElementTypeStaticText"
            );

    private final By unreadIndicator =
            AppiumBy.iOSNsPredicateString(
                    "name CONTAINS 'unread' OR label CONTAINS 'unread'"
            );

    private final By markAllReadButton =
            AppiumBy.accessibilityId("mark_all_read");

    private final By backButton =
            AppiumBy.accessibilityId("Back");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public IosNotificationCenterScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver =
                (IOSDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify Notification Center is visible.
     */
    public IosNotificationCenterScreen assertVisible() {

        log.info("🔔 Verifying iOS Notification Center");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        notificationCenterRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "iOS Notification Center is not visible"
        );

        return this;
    }

    /**
     * Assert at least one notification exists.
     */
    public IosNotificationCenterScreen assertNotificationsPresent() {

        log.info("🔎 Checking notifications presence");

        List<WebElement> notifications =
                driver.findElements(notificationCell);

        assertFalse(
                notifications.isEmpty(),
                "No notifications found in iOS Notification Center"
        );

        return this;
    }

    /**
     * Assert at least one unread notification exists.
     */
    public IosNotificationCenterScreen assertUnreadPresent() {

        log.info("📬 Checking unread notifications");

        List<WebElement> unread =
                driver.findElements(unreadIndicator);

        assertFalse(
                unread.isEmpty(),
                "Expected unread notifications but none found"
        );

        return this;
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    /**
     * Open the first notification.
     */
    public IosNotificationCenterScreen openFirstNotification() {

        log.info("📨 Opening first notification");

        List<WebElement> notifications =
                WaitUtils.waitForAllVisible(
                        driver,
                        notificationCell,
                        10
                );

        notifications.get(0).click();
        return this;
    }

    /**
     * Mark all notifications as read (if available).
     */
    public IosNotificationCenterScreen markAllAsRead() {

        log.info("✅ Marking all notifications as read");

        if (isElementPresent(markAllReadButton, 3)) {
            driver.findElement(markAllReadButton).click();
        }

        return this;
    }

    /**
     * Navigate back to Home screen.
     */
    public IosHomeScreen goBackToHome() {

        log.info("⬅️ Navigating back to Home");

        WaitUtils.waitForClickable(
                driver,
                backButton,
                5
        ).click();

        return new IosHomeScreen();
    }

    /* =========================================================
       CONTENT VALIDATION
       ========================================================= */

    /**
     * Assert a notification with a given title exists.
     */
    public IosNotificationCenterScreen assertNotificationWithTitle(
            String expectedTitle
    ) {

        log.info("🔍 Validating notification title={}",
                expectedTitle);

        List<WebElement> titles =
                driver.findElements(notificationTitle);

        boolean found =
                titles.stream()
                        .anyMatch(t ->
                                expectedTitle.equals(t.getText()));

        assertTrue(
                found,
                "Notification with title '" +
                        expectedTitle +
                        "' not found"
        );

        return this;
    }

    /* =========================================================
       UTILITIES
       ========================================================= */

    protected boolean isElementPresent(By locator, int seconds) {
        try {
            WaitUtils.waitForVisible(
                    driver,
                    locator,
                    seconds
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
