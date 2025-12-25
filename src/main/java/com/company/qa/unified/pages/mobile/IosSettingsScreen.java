package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IosSettingsScreen
 *
 * Represents the Settings / Profile screen on iOS.
 *
 * Covers:
 * - Profile validation
 * - Notification & privacy navigation
 * - Subscription entry
 * - Logout flow
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class IosSettingsScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(IosSettingsScreen.class);

    private final IOSDriver driver;

    /* =========================================================
       LOCATORS (XCUI)
       ========================================================= */

    private final By settingsRoot =
            AppiumBy.accessibilityId("settings_root");

    private final By profileName =
            AppiumBy.accessibilityId("profile_name");

    private final By profilePhone =
            AppiumBy.accessibilityId("profile_phone");

    private final By notificationsOption =
            AppiumBy.accessibilityId("settings_notifications");

    private final By privacyOption =
            AppiumBy.accessibilityId("settings_privacy");

    private final By subscriptionOption =
            AppiumBy.accessibilityId("settings_subscription");

    private final By logoutButton =
            AppiumBy.accessibilityId("logout_button");

    private final By confirmLogoutButton =
            AppiumBy.accessibilityId("confirm_logout");

    private final By backButton =
            AppiumBy.accessibilityId("Back");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public IosSettingsScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver =
                (IOSDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify Settings screen is visible.
     */
    public IosSettingsScreen assertVisible() {

        log.info("⚙️ Verifying iOS Settings screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        settingsRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "iOS Settings screen is not visible"
        );

        return this;
    }

    /**
     * Validate profile name and phone number.
     */
    public IosSettingsScreen assertProfileDetails(
            String expectedName,
            String expectedPhoneSuffix
    ) {

        log.info("👤 Validating profile details");

        String actualName =
                WaitUtils.waitForVisible(
                        driver,
                        profileName,
                        5
                ).getText();

        String actualPhone =
                WaitUtils.waitForVisible(
                        driver,
                        profilePhone,
                        5
                ).getText();

        assertEquals(
                expectedName,
                actualName,
                "Profile name mismatch"
        );

        assertTrue(
                actualPhone.contains(expectedPhoneSuffix),
                "Profile phone mismatch"
        );

        return this;
    }

    /* =========================================================
       NAVIGATION
       ========================================================= */

    /**
     * Open notification settings.
     */
    public IosSettingsScreen openNotificationSettings() {

        log.info("🔔 Opening notification settings");

        WaitUtils.waitForClickable(
                driver,
                notificationsOption,
                5
        ).click();

        return this;
    }

    /**
     * Open privacy settings.
     */
    public IosSettingsScreen openPrivacySettings() {

        log.info("🛡 Opening privacy settings");

        WaitUtils.waitForClickable(
                driver,
                privacyOption,
                5
        ).click();

        return this;
    }

    /**
     * Open subscription settings.
     */
    public IosSettingsScreen openSubscriptionSettings() {

        log.info("💎 Opening subscription settings");

        WaitUtils.waitForClickable(
                driver,
                subscriptionOption,
                5
        ).click();

        return this;
    }

    /* =========================================================
       LOGOUT FLOW
       ========================================================= */

    /**
     * Logout from the app.
     */
    public IosLoginScreen logout() {

        log.info("🚪 Logging out");

        scrollToLogoutIfNeeded();

        WaitUtils.waitForClickable(
                driver,
                logoutButton,
                5
        ).click();

        WaitUtils.waitForClickable(
                driver,
                confirmLogoutButton,
                5
        ).click();

        return new IosLoginScreen();
    }

    /* =========================================================
       NAVIGATION BACK
       ========================================================= */

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
       UTILITIES
       ========================================================= */

    /**
     * Scroll down until logout button is visible.
     */
    private void scrollToLogoutIfNeeded() {

        log.info("⬇️ Scrolling to logout option");

        List<WebElement> elements =
                driver.findElements(logoutButton);

        if (!elements.isEmpty()) {
            return;
        }

        swipeUp();
    }
}
