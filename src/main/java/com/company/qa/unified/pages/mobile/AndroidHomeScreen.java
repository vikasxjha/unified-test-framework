package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AndroidHomeScreen
 *
 * Represents the main landing screen of the Android app.
 *
 * Covers:
 * - Search entry
 * - Navigation to notifications
 * - Profile access
 * - Permission handling
 * - High-level assertions
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class AndroidHomeScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(AndroidHomeScreen.class);

    private final AndroidDriver driver;

    /* =========================================================
       LOCATORS
       ========================================================= */

    private final By searchBox =
            By.id("com.truecaller:id/searchText");

    private final By profileIcon =
            By.id("com.truecaller:id/profileIcon");

    private final By notificationBell =
            By.id("com.truecaller:id/notificationIcon");

    private final By permissionAllowButton =
            By.id("com.android.permissioncontroller:id/permission_allow_button");

    private final By homeRoot =
            By.id("com.truecaller:id/homeRoot");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public AndroidHomeScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver = (AndroidDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify that Home screen is visible.
     */
    public AndroidHomeScreen assertHomeVisible() {

        log.info("📱 Verifying Android Home screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        homeRoot,
                        10
                );

        assertTrue(root.isDisplayed(),
                "Home screen is not visible");

        return this;
    }

    /**
     * Verify user is logged in.
     */
    public AndroidHomeScreen assertUserLoggedIn() {

        log.info("✅ Verifying user logged in");

        // Check that profile icon is visible (indicates logged in state)
        WebElement profile =
                WaitUtils.waitForVisible(
                        driver,
                        profileIcon,
                        10
                );

        assertTrue(profile.isDisplayed(),
                "User not logged in - profile icon not visible");

        return this;
    }

    /**
     * Navigate to login screen.
     */
    public AndroidLoginScreen goToLogin() {

        log.info("🔐 Navigating to login screen");

        // Assuming there's a login button or action
        // For now, just return new login screen
        return new AndroidLoginScreen();
    }

    /**
     * Navigate to profile.
     */
    public AndroidSettingsScreen goToProfile() {

        log.info("👤 Navigating to profile");

        WaitUtils.waitForClickable(
                driver,
                profileIcon,
                10
        ).click();

        return new AndroidSettingsScreen();
    }

    /**
     * Navigate to notifications.
     */
    public AndroidNotificationCenterScreen goToNotifications() {

        log.info("🔔 Navigating to notifications");

        WaitUtils.waitForClickable(
                driver,
                notificationBell,
                10
        ).click();

        return new AndroidNotificationCenterScreen();
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    /**
     * Tap on search box.
     */
    public AndroidHomeScreen tapSearch() {

        log.info("🔍 Tapping search box");

        WaitUtils.waitForClickable(
                driver,
                searchBox,
                10
        ).click();

        return this;
    }

    /**
     * Enter search text.
     */
    public AndroidHomeScreen enterSearchQuery(String query) {

        log.info("⌨️ Entering search query={}", query);

        WebElement box =
                WaitUtils.waitForVisible(
                        driver,
                        searchBox,
                        10
                );

        box.clear();
        box.sendKeys(query);

        driver.pressKey(
                new KeyEvent(AndroidKey.ENTER)
        );

        return this;
    }

    /**
     * Open notifications center.
     */
    public AndroidNotificationCenterScreen openNotifications() {

        log.info("🔔 Opening notification center");

        WaitUtils.waitForClickable(
                driver,
                notificationBell,
                10
        ).click();

        return new AndroidNotificationCenterScreen();
    }

    /**
     * Open profile screen.
     */
    public AndroidSettingsScreen openProfile() {

        log.info("👤 Opening profile/settings");

        WaitUtils.waitForClickable(
                driver,
                profileIcon,
                10
        ).click();

        return new AndroidSettingsScreen();
    }

    /* =========================================================
       PERMISSIONS
       ========================================================= */

    /**
     * Handle runtime permission dialog if present.
     */
    public AndroidHomeScreen allowPermissionIfPresent() {

        log.info("🔐 Checking runtime permission dialog");

        if (isElementPresent(permissionAllowButton, 3)) {
            log.info("Granting permission");
            driver.findElement(permissionAllowButton).click();
        }

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
