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
 * AndroidLoginScreen
 *
 * Represents the login / onboarding screen on Android.
 *
 * Covers:
 * - Phone number entry
 * - Country code selection
 * - OTP trigger
 * - Navigation to OTP verification screen
 *
 * RULE:
 * ❌ Tests must NOT use driver directly
 * ✅ Tests must ALWAYS go through screen objects
 */
public class AndroidLoginScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(AndroidLoginScreen.class);

    private final AndroidDriver driver;

    /* =========================================================
       LOCATORS
       ========================================================= */

    private final By phoneInput =
            By.id("com.truecaller:id/phoneNumber");

    private final By continueButton =
            By.id("com.truecaller:id/continueButton");

    private final By countryPicker =
            By.id("com.truecaller:id/countryPicker");

    private final By loginRoot =
            By.id("com.truecaller:id/loginRoot");

    private final By permissionAllowButton =
            By.id("com.android.permissioncontroller:id/permission_allow_button");

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    public AndroidLoginScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver = (AndroidDriver) AppiumDriverFactory.getDriver();
    }

    /* =========================================================
       SCREEN ASSERTIONS
       ========================================================= */

    /**
     * Verify login screen is visible.
     */
    public AndroidLoginScreen assertLoginVisible() {

        log.info("📱 Verifying Android Login screen");

        WebElement root =
                WaitUtils.waitForVisible(
                        driver,
                        loginRoot,
                        10
                );

        assertTrue(root.isDisplayed(),
                "Login screen is not visible");

        return this;
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    /**
     * Select country code (if required).
     */
    public AndroidLoginScreen selectCountryIfNeeded(String countryName) {

        log.info("🌍 Selecting country={}", countryName);

        WaitUtils.waitForClickable(
                driver,
                countryPicker,
                5
        ).click();

        By country =
                By.xpath(
                        "//*[@text='" + countryName + "']"
                );

        WaitUtils.waitForClickable(
                driver,
                country,
                10
        ).click();

        return this;
    }

    /**
     * Enter phone number.
     */
    public AndroidLoginScreen enterPhoneNumber(String phoneNumber) {

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

        driver.pressKey(
                new KeyEvent(AndroidKey.ENTER)
        );

        return this;
    }

    /**
     * Submit login and request OTP.
     */
    public AndroidOtpVerificationScreen submitLogin() {

        log.info("➡️ Submitting login / requesting OTP");

        WaitUtils.waitForClickable(
                driver,
                continueButton,
                10
        ).click();

        allowPermissionIfPresent();

        return new AndroidOtpVerificationScreen();
    }

    /* =========================================================
       PERMISSIONS
       ========================================================= */

    /**
     * Handle runtime permission dialog if present.
     */
    private void allowPermissionIfPresent() {

        log.info("🔐 Checking permission dialog");

        try {
            WebElement allow =
                    WaitUtils.waitForVisible(
                            driver,
                            permissionAllowButton,
                            3
                    );
            allow.click();
        } catch (Exception ignored) {
            // Permission dialog not shown
        }
    }
}
