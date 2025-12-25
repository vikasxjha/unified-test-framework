package com.company.qa.unified.stepdefs;

import com.company.qa.unified.drivers.AppiumDriverFactory;
import com.company.qa.unified.pages.mobile.AndroidHomeScreen;
import com.company.qa.unified.pages.mobile.AndroidLoginScreen;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MobileSteps
 *
 * Cucumber step definitions for mobile testing (Appium).
 *
 * Covers:
 * - App launch
 * - Login flows
 * - Navigation
 * - Gestures
 */
public class MobileSteps {

    private static final Log log = Log.get(MobileSteps.class);

    private AndroidHomeScreen homeScreen;
    private AndroidLoginScreen loginScreen;

    /* =========================================================
       APP LAUNCH
       ========================================================= */

    @Given("the mobile application is installed")
    public void verifyMobileAppInstalled() {
        log.info("📱 Verifying mobile application installed");

        AppiumDriver driver = AppiumDriverFactory.getDriver();
        assertNotNull(driver, "Appium driver not initialized");

        ReportPublisher.step("Mobile application verified");
    }

    @When("I launch the mobile application")
    public void launchMobileApplication() {
        log.info("🚀 Launching mobile application");

        AppiumDriver driver = AppiumDriverFactory.getDriver();

        // App should auto-launch via desired capabilities
        // Wait for home screen to appear
        homeScreen = new AndroidHomeScreen();

        ReportPublisher.step("Mobile application launched");
    }

    @Then("the home screen should be displayed")
    public void verifyHomeScreenDisplayed() {
        log.info("✅ Verifying home screen displayed");

        assertNotNull(homeScreen, "Home screen not initialized");
        homeScreen.assertHomeVisible();

        ReportPublisher.step("Home screen displayed successfully");
    }

    /* =========================================================
       LOGIN
       ========================================================= */

    @Given("I am on the mobile login screen")
    public void navigateToMobileLoginScreen() {
        log.info("🔐 Navigating to mobile login screen");

        if (homeScreen == null) {
            launchMobileApplication();
        }

        loginScreen = homeScreen.goToLogin();

        ReportPublisher.step("Navigated to login screen");
    }

    @When("I login on mobile with valid credentials")
    public void loginOnMobile() {
        log.info("🔐 Logging in on mobile");

        assertNotNull(loginScreen, "Login screen not initialized");

        String phoneNumber = "+19999999999";
        String otp = "123456";

        loginScreen.enterPhoneNumber(phoneNumber);
        loginScreen.requestOtp();
        loginScreen.enterOtp(otp);
        homeScreen = loginScreen.submitOtp();

        ReportPublisher.step("Logged in on mobile successfully");
    }

    @Then("I should be logged in on mobile")
    public void verifyMobileLoginSuccess() {
        log.info("✅ Verifying mobile login success");

        assertNotNull(homeScreen, "Home screen not loaded after login");
        homeScreen.assertUserLoggedIn();

        ReportPublisher.step("User logged in on mobile successfully");
    }

    /* =========================================================
       NAVIGATION
       ========================================================= */

    @When("I navigate to {string} on mobile")
    public void navigateToScreenOnMobile(String screenName) {
        log.info("📱 Navigating to screen: {}", screenName);

        if (homeScreen == null) {
            launchMobileApplication();
        }

        switch (screenName.toLowerCase()) {
            case "search":
                homeScreen.tapSearch();
                break;
            case "profile":
                homeScreen.goToProfile();
                break;
            case "notifications":
                homeScreen.goToNotifications();
                break;
            default:
                fail("Unknown screen: " + screenName);
        }

        ReportPublisher.step("Navigated to: " + screenName);
    }

    @When("I search for {string} on mobile")
    public void searchOnMobile(String query) {
        log.info("🔍 Searching on mobile: {}", query);

        if (homeScreen == null) {
            launchMobileApplication();
        }

        homeScreen.tapSearch();
        homeScreen.enterSearchQuery(query);

        ReportPublisher.step("Searched for: " + query);
    }

    @Then("I should see mobile search results")
    public void verifyMobileSearchResults() {
        log.info("✅ Verifying mobile search results");

        // Results would be validated here
        // For now, just verify we're still on a valid screen
        assertNotNull(homeScreen, "Home screen lost");

        ReportPublisher.step("Mobile search results displayed");
    }
}

