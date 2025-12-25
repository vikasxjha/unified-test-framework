package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IosLoginScreen
 *
 * Represents the login / onboarding screen on iOS.
 *
 * Covers:
 * - Phone number entry
 * - Country selection
 * - OTP trigger
 * - Permission handling
 * - Navigation to OTP verification screen
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class IosLoginScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(IosLoginScreen.class);

    private final IOSDriver driver;

    /* =========================================================
       LOCATORS (XCUI)
       ========================================================= */

    private final By loginRoot =
            AppiumBy.accessibilityId("login_root");

    private final By countryPicker =
            AppiumBy.accessibilityId("country_picker");

    private final By phoneInput =
            AppiumBy.accessibilityId("phone_input");

    private final By continueButton =
            AppiumBy.accessibilityId("continue_button");

    private final By allowPermissionButton =
            AppiumBy.iOSNsPredicateString(
                    "label == 'Allow' OR label == 'Allow While Using App'"
            );

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public IosLoginScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver =
                (IOSDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify login screen is visible.
     */
    public IosLoginScreen assertLoginVisible() {

        log.info("📱 Verifying iOS Login screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        loginRoot,
                        10
                );

        assertTrue(
                root.isDisplayed(),
                "iOS Login screen is not visible"
        );

        return this;
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    /**
     * Select country if picker is shown.
     */
    public IosLoginScreen selectCountryIfNeeded(String countryName) {

        log.info("🌍 Selecting country={}", countryName);

        try {
            WaitUtils.waitForClickable(
                    driver,
                    countryPicker,
                    3
            ).click();

            By country =
                    MobileBy.iOSNsPredicateString(
                            "label == '" + countryName + "'"
                    );

            WaitUtils.waitForClickable(
                    driver,
                    country,
                    10
            ).click();

        } catch (Exception ignored) {
            // Country picker not shown
        }

        return this;
    }

    /**
     * Enter phone number.
     */
    public IosLoginScreen enterPhoneNumber(String phoneNumber) {

        log.info("📞 Entering phone number=****{}",
                phoneNumber.substring(
                        Math.max(0, phoneNumber.length() - 4)));

        WebElement phone =
                WaitUtils.waitForVisible(
                        driver,
                        phoneInput,
                        10
                );

        phone.clear();
        phone.sendKeys(phoneNumber);

        driver.getKeyboard().pressKey("\n");

        return this;
    }

    /**
     * Submit login and request OTP.
     */
    public IosOtpVerificationScreen submitLogin() {

        log.info("➡️ Submitting login / requesting OTP");

        WaitUtils.waitForClickable(
                driver,
                continueButton,
                10
        ).click();

        allowPermissionIfPresent();

        return new IosOtpVerificationScreen();
    }

    /* =========================================================
       PERMISSIONS
       ========================================================= */

    /**
     * Handle iOS system permission dialog if present.
     */
    private void allowPermissionIfPresent() {

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
    }
}
