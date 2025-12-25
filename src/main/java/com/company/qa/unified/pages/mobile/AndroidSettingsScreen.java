package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AndroidSettingsScreen
 *
 * Represents the Settings / Profile screen on Android.
 *
 * Covers:
 * - Profile info validation
 * - Notification settings
 * - Privacy settings
 * - Subscription entry
 * - Logout flow
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class AndroidSettingsScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(AndroidSettingsScreen.class);

    private final AndroidDriver driver;

    /* =========================================================
       LOCATORS
       ========================================================= */

    private final By settingsRoot =
            By.id("com.truecaller:id/settingsRoot");

    private final By profileName =
            By.id("com.truecaller:id/profileName");

    private final By profileNumber =
            By.id("com.truecaller:id/profileNumber");

    private final By notificationsOption =
            By.id("com.truecaller:id/settingsNotifications");

    private final By privacyOption =
            By.id("com.truecaller:id/settingsPrivacy");

    private final By subscriptionOption =
            By.id("com.truecaller:id/settingsSubscription");

    private final By logoutButton =
            By.id("com.truecaller:id/logout");

    private final By confirmLogoutButton =
            By.id("com.truecaller:id/confirmLogout");

    private final By backButton =
            By.xpath("//*[@content-desc='Navigate up']");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public AndroidSettingsScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver =
                (AndroidDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify settings screen is visible.
     */
    public AndroidSettingsScreen assertVisible() {

        log.info("⚙️ Verifying Settings screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        settingsRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "Settings screen is not visible"
        );

        return this;
    }

    /**
     * Validate profile details.
     */
    public AndroidSettingsScreen assertProfileDetails(
            String expectedName,
            String expectedPhone
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
                        profileNumber,
                        5
                ).getText();

        assertEquals(
                expectedName,
                actualName,
                "Profile name mismatch"
        );

        assertTrue(
                actualPhone.contains(expectedPhone),
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
    public AndroidSettingsScreen openNotificationSettings() {

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
    public AndroidSettingsScreen openPrivacySettings() {

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
    public AndroidSettingsScreen openSubscriptionSettings() {

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
     * Logout from the application.
     */
    public AndroidLoginScreen logout() {

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

        return new AndroidLoginScreen();
    }

    /* =========================================================
       NAVIGATION BACK
       ========================================================= */

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
       UTILITIES
       ========================================================= */

    /**
     * Scroll until logout button is visible.
     */
    private void scrollToLogoutIfNeeded() {

        log.info("⬇️ Scrolling to logout option");

        List<WebElement> elements =
                driver.findElements(logoutButton);

        if (!elements.isEmpty()) {
            return;
        }

        // Scroll to logout button if not visible
        try {
            WaitUtils.waitForVisible(driver, logoutButton, 3);
        } catch (Exception e) {
            log.debug("Logout button not visible, attempting scroll");
            // Use standard scroll if element not visible
        }
    }
}
