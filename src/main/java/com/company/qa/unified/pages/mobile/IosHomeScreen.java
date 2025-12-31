package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IosHomeScreen
 *
 * Represents the main landing screen of the iOS app.
 *
 * Covers:
 * - Search entry
 * - Notification center navigation
 * - Profile/settings access
 * - iOS permission handling
 * - Home screen assertions
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class IosHomeScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(IosHomeScreen.class);

    private final AppiumDriver driver;

    /* =========================================================
       LOCATORS (XCUI)
       ========================================================= */

    private final By homeRoot =
            AppiumBy.accessibilityId("home_root");

    private final By searchBox =
            AppiumBy.accessibilityId("search_input");

    private final By notificationBell =
            AppiumBy.accessibilityId("notifications_button");

    private final By profileIcon =
            AppiumBy.accessibilityId("profile_button");

    private final By allowPermissionButton =
            AppiumBy.iOSNsPredicateString(
                    "label == 'Allow' OR label == 'Allow While Using App'"
            );

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public IosHomeScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver = AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify that Home screen is visible.
     */
    public IosHomeScreen assertHomeVisible() {

        log.info("📱 Verifying iOS Home screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        homeRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "iOS Home screen is not visible"
        );

        return this;
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    /**
     * Tap on search box.
     */
    public IosHomeScreen tapSearch() {

        log.info("🔍 Tapping search box");

        WaitUtils.waitForClickable(
                driver,
                searchBox,
                10
        ).click();

        return this;
    }

    /**
     * Enter search query.
     */
    public IosHomeScreen enterSearchQuery(String query) {

        log.info("⌨️ Entering search query={}", query);

        WebElement box =
                WaitUtils.waitForVisible(
                        driver,
                        searchBox,
                        10
                );

        box.clear();
        box.sendKeys(query);
        box.sendKeys("\n"); // Press return/enter key

        return this;
    }

    /**
     * Open Notification Center.
     */
    public IosNotificationCenterScreen openNotifications() {

        log.info("🔔 Opening Notification Center");

        WaitUtils.waitForClickable(
                driver,
                notificationBell,
                10
        ).click();

        return new IosNotificationCenterScreen();
    }

    /**
     * Open Profile / Settings screen.
     */
    public IosSettingsScreen openProfile() {

        log.info("👤 Opening Profile / Settings");

        WaitUtils.waitForClickable(
                driver,
                profileIcon,
                10
        ).click();

        return new IosSettingsScreen();
    }

    /* =========================================================
       PERMISSIONS
       ========================================================= */

    /**
     * Handle iOS system permission dialog if present.
     */
    public IosHomeScreen allowPermissionIfPresent() {

        log.info("🔐 Checking iOS permission dialog");

        try {
            WebElement allow =
                    WaitUtils.waitForVisible(
                            driver,
                            allowPermissionButton,
                            3
                    );
            allow.click();
            log.info("Permission granted");
        } catch (Exception ignored) {
            // Permission dialog not shown
        }

        return this;
    }
}
