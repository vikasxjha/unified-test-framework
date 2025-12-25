package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AndroidOtpVerificationScreen
 *
 * Represents the OTP verification screen on Android.
 */
public class AndroidOtpVerificationScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(AndroidOtpVerificationScreen.class);

    private final AndroidDriver driver;

    /* Locators */
    private final By otpInput = By.id("com.truecaller:id/otpInput");
    private final By verifyButton = By.id("com.truecaller:id/verifyButton");

    public AndroidOtpVerificationScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver = (AndroidDriver) AppiumDriverFactory.getDriver();
    }

    public AndroidOtpVerificationScreen enterOtp(String otp) {
        log.info("🔢 Entering OTP");

        WebElement input = WaitUtils.waitForVisible(driver, otpInput, 10);
        input.clear();
        input.sendKeys(otp);

        return this;
    }

    public AndroidHomeScreen submitOtp() {
        log.info("✅ Submitting OTP");

        WaitUtils.waitForClickable(driver, verifyButton, 10).click();

        return new AndroidHomeScreen();
    }
}

