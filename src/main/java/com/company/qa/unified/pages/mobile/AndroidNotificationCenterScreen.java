package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AndroidNotificationCenterScreen
 *
 * Represents the in-app Notification Center on Android.
 *
 * Covers:
 * - Viewing notifications
 * - Read / unread state
 * - Mark as read
 * - Open notification
 * - High-level assertions
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class AndroidNotificationCenterScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(AndroidNotificationCenterScreen.class);

    private final AndroidDriver driver;

    /* =========================================================
       LOCATORS
       ========================================================= */

    private final By notificationCenterRoot =
            By.id("com.truecaller:id/notificationCenterRoot");

    private final By notificationItem =
            By.id("com.truecaller:id/notificationItem");

    private final By notificationTitle =
            By.id("com.truecaller:id/notificationTitle");

    private final By unreadIndicator =
            By.id("com.truecaller:id/unreadDot");

    private final By markAllReadButton =
            By.id("com.truecaller:id/markAllRead");

    private final By backButton =
            By.xpath("//*[@content-desc='Navigate up']");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public AndroidNotificationCenterScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver =
                (AndroidDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify notification center is visible.
     */
    public AndroidNotificationCenterScreen assertVisible() {

        log.info("🔔 Verifying Notification Center screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        notificationCenterRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "Notification Center is not visible"
        );

        return this;
    }

    /**
     * Assert that at least one notification is present.
     */
    public AndroidNotificationCenterScreen assertNotificationsPresent() {

        log.info("🔎 Checking notifications presence");

        List<WebElement> notifications =
                driver.findElements(notificationItem);

        assertFalse(
                notifications.isEmpty(),
                "No notifications found in Notification Center"
        );

        return this;
    }

    /**
     * Assert at least one unread notification exists.
     */
    public AndroidNotificationCenterScreen assertUnreadPresent() {

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
    public AndroidNotificationCenterScreen openFirstNotification() {

        log.info("📨 Opening first notification");

        List<WebElement> notifications =
                WaitUtils.waitForAllVisible(
                        driver,
                        notificationItem,
                        10
                );

        notifications.get(0).click();

        return this;
    }

    /**
     * Mark all notifications as read.
     */
    public AndroidNotificationCenterScreen markAllAsRead() {

        log.info("✅ Marking all notifications as read");

        if (isElementPresent(markAllReadButton, 3)) {
            driver.findElement(markAllReadButton).click();
        }

        return this;
    }

    /**
     * Navigate back to Home screen.
     */
    public AndroidHomeScreen goBackToHome() {

        log.info("⬅️ Navigating back to Home");

        WaitUtils.waitForClickable(
                driver,
                backButton,
                5
        ).click();

        return new AndroidHomeScreen();
    }

    /* =========================================================
       CONTENT VALIDATION
       ========================================================= */

    /**
     * Assert a notification with a given title exists.
     */
    public AndroidNotificationCenterScreen assertNotificationWithTitle(
            String expectedTitle
    ) {

        log.info("🔍 Validating notification title={}",
                expectedTitle);

        List<WebElement> titles =
                driver.findElements(notificationTitle);

        boolean found =
                titles.stream()
                        .anyMatch(
                                t -> expectedTitle.equals(
                                        t.getText()
                                )
                        );

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
