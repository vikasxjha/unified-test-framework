package com.company.qa.unified.pages.mobile;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.WaitUtils;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IosOtpVerificationScreen
 *
 * Represents the OTP verification screen on iOS.
 */
public class IosOtpVerificationScreen extends BaseMobileScreen {

    private static final Log log =
            Log.get(IosOtpVerificationScreen.class);

    private final IOSDriver driver;

    /* Locators */
    private final By otpInput = By.id("otpInput");
    private final By verifyButton = By.id("verifyButton");

    public IosOtpVerificationScreen() {
        super(AppiumDriverFactory.getDriver());
        this.driver = (IOSDriver) AppiumDriverFactory.getDriver();
    }

    public IosOtpVerificationScreen enterOtp(String otp) {
        log.info("🔢 Entering OTP");

        WebElement input = WaitUtils.waitForVisible(driver, otpInput, 10);
        input.clear();
        input.sendKeys(otp);

        return this;
    }

    public IosHomeScreen submitOtp() {
        log.info("✅ Submitting OTP");

        WaitUtils.waitForClickable(driver, verifyButton, 10).click();

        return new IosHomeScreen();
    }
}

