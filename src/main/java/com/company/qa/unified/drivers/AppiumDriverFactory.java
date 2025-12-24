package com.company.qa.unified.drivers;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.config.RuntimeConfig;
import com.company.qa.unified.utils.Log;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;

/**
 * Central factory for Appium drivers.
 *
 * Supports:
 * - Android & iOS
 * - Local Appium
 * - Remote device farms (BrowserStack / SauceLabs)
 * - Thread-safe execution
 *
 * RULE:
 * ❌ Tests must NOT instantiate AppiumDriver directly
 * ✅ Tests must ALWAYS use AppiumDriverFactory
 */
public final class AppiumDriverFactory {

    private static final Log log =
            Log.get(AppiumDriverFactory.class);

    private static final ThreadLocal<AppiumDriver> DRIVER =
            new ThreadLocal<>();

    private static final EnvironmentConfig ENV =
            EnvironmentConfig.get();

    private AppiumDriverFactory() {
        // utility
    }

    /* =========================================================
       PUBLIC API
       ========================================================= */

    public static AppiumDriver getDriver() {
        if (DRIVER.get() == null) {
            throw new IllegalStateException(
                    "AppiumDriver not initialized. Call initDriver() first.");
        }
        return DRIVER.get();
    }

    public static void initDriver() {
        if (DRIVER.get() != null) {
            return;
        }

        try {
            String platform =
                    System.getProperty("platform", "android").toLowerCase();

            AppiumDriver driver = switch (platform) {
                case "android" -> createAndroidDriver();
                case "ios" -> createIosDriver();
                default -> throw new IllegalArgumentException(
                        "Unsupported platform: " + platform);
            };

            driver.manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(5));

            DRIVER.set(driver);

            log.info("📱 Appium driver started for platform={}", platform);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize Appium driver", e);
        }
    }

    public static void quitDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            log.info("📱 Appium driver quit");
        }
    }

    /* =========================================================
       ANDROID DRIVER
       ========================================================= */

    private static AndroidDriver createAndroidDriver()
            throws Exception {

        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", "Android");
        caps.setCapability("automationName", "UiAutomator2");
        caps.setCapability("deviceName",
                System.getProperty("deviceName", "Android Emulator"));
        caps.setCapability("appPackage",
                System.getProperty("appPackage", "com.truecaller"));
        caps.setCapability("appActivity",
                System.getProperty("appActivity", ".ui.MainActivity"));
        caps.setCapability("noReset", true);

        applyCommonCapabilities(caps);

        URL serverUrl = getServerUrl();
        return new AndroidDriver(serverUrl, caps);
    }

    /* =========================================================
       IOS DRIVER
       ========================================================= */

    private static IOSDriver createIosDriver()
            throws Exception {

        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", "iOS");
        caps.setCapability("automationName", "XCUITest");
        caps.setCapability("deviceName",
                System.getProperty("deviceName", "iPhone 15"));
        caps.setCapability("platformVersion",
                System.getProperty("platformVersion", "17.0"));
        caps.setCapability("bundleId",
                System.getProperty("bundleId", "com.truecaller.app"));
        caps.setCapability("noReset", true);

        applyCommonCapabilities(caps);

        URL serverUrl = getServerUrl();
        return new IOSDriver(serverUrl, caps);
    }

    /* =========================================================
       COMMON CAPABILITIES
       ========================================================= */

    private static void applyCommonCapabilities(
            DesiredCapabilities caps
    ) {
        caps.setCapability("newCommandTimeout", 300);

        if (RuntimeConfig.recordVideo()) {
            caps.setCapability("recordVideo", true);
        }

        if (RuntimeConfig.environment().isProd()) {
            caps.setCapability("autoGrantPermissions", false);
        } else {
            caps.setCapability("autoGrantPermissions", true);
        }
    }

    /* =========================================================
       APPIUM SERVER
       ========================================================= */

    private static URL getServerUrl()
            throws Exception {

        String remoteUrl =
                System.getProperty("appium.remote.url");

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            log.info("Using remote Appium server: {}", remoteUrl);
            return new URL(remoteUrl);
        }

        String localUrl =
                System.getProperty(
                        "appium.local.url",
                        "http://127.0.0.1:4723/wd/hub"
                );

        log.info("Using local Appium server: {}", localUrl);
        return new URL(localUrl);
    }
}
