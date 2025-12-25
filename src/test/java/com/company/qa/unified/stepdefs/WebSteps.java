package com.company.qa.unified.stepdefs;

import com.company.qa.unified.config.EnvironmentConfig;
import com.company.qa.unified.drivers.PlaywrightDriverFactory;
import com.company.qa.unified.pages.web.HomePage;
import com.company.qa.unified.pages.web.LoginPage;
import com.company.qa.unified.pages.web.SearchPage;
import com.company.qa.unified.pages.web.PricingPage;
import com.company.qa.unified.pages.web.CheckoutPage;
import com.company.qa.unified.utils.Log;
import com.company.qa.unified.utils.ReportPublisher;
import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSteps
 *
 * Cucumber step definitions for web testing (Playwright).
 *
 * Covers:
 * - Navigation
 * - Login flows
 * - Search
 * - Subscription/upgrade
 */
public class WebSteps {

    private static final Log log = Log.get(WebSteps.class);

    private HomePage homePage;
    private LoginPage loginPage;
    private SearchPage searchPage;
    private PricingPage pricingPage;
    private CheckoutPage checkoutPage;

    /* =========================================================
       HOME PAGE
       ========================================================= */

    @Given("I open the web application")
    public void openWebApplication() {
        log.info("🌐 Opening web application");

        Page page = PlaywrightDriverFactory.getPage();
        String baseUrl = EnvironmentConfig.get().getWebBaseUrl();

        page.navigate(baseUrl);

        homePage = new HomePage(page);

        ReportPublisher.step("Opened web application: " + baseUrl);
    }

    @Then("the home page should load successfully")
    public void verifyHomePageLoaded() {
        log.info("✅ Verifying home page loaded");

        assertNotNull(homePage, "Home page not initialized");
        homePage.assertPageLoaded();

        ReportPublisher.step("Home page loaded successfully");
    }

    @Then("the page title should contain {string}")
    public void verifyPageTitleContains(String expectedTitle) {
        log.info("✅ Verifying page title contains: {}", expectedTitle);

        Page page = PlaywrightDriverFactory.getPage();
        String actualTitle = page.title();

        assertTrue(
            actualTitle.contains(expectedTitle),
            "Expected title to contain '" + expectedTitle + "' but was '" + actualTitle + "'"
        );

        ReportPublisher.step("Page title verified: " + actualTitle);
    }

    /* =========================================================
       LOGIN PAGE
       ========================================================= */

    @Given("I open the login page")
    public void openLoginPage() {
        log.info("🔐 Opening login page");

        Page page = PlaywrightDriverFactory.getPage();
        String baseUrl = EnvironmentConfig.get().getWebBaseUrl();

        page.navigate(baseUrl + "/login");

        loginPage = new LoginPage(page);

        ReportPublisher.step("Opened login page");
    }

    @Then("the login page should be displayed")
    public void verifyLoginPageDisplayed() {
        log.info("✅ Verifying login page displayed");

        assertNotNull(loginPage, "Login page not initialized");
        loginPage.assertPageLoaded();

        ReportPublisher.step("Login page displayed successfully");
    }

    @When("I login with valid credentials")
    public void loginWithValidCredentials() {
        log.info("🔐 Logging in with valid credentials");

        String email = EnvironmentConfig.get().getTestUser();
        String password = EnvironmentConfig.get().getTestPassword();

        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        homePage = loginPage.clickLogin();

        ReportPublisher.step("Logged in successfully");
    }

    @Then("I should be logged in successfully")
    public void verifyLoggedIn() {
        log.info("✅ Verifying user logged in");

        assertNotNull(homePage, "Home page not loaded after login");
        homePage.assertUserLoggedIn();

        ReportPublisher.step("User logged in successfully");
    }

    /* =========================================================
       SEARCH
       ========================================================= */

    @When("I search for {string}")
    public void searchFor(String query) {
        log.info("🔍 Searching for: {}", query);

        if (homePage == null) {
            openWebApplication();
        }

        searchPage = homePage.search(query);

        ReportPublisher.step("Searched for: " + query);
    }

    @Then("I should see search results")
    public void verifySearchResults() {
        log.info("✅ Verifying search results");

        assertNotNull(searchPage, "Search page not loaded");
        searchPage.assertResultsDisplayed();

        ReportPublisher.step("Search results displayed");
    }

    /* =========================================================
       UPGRADE / PREMIUM
       ========================================================= */

    @When("I navigate to pricing page")
    public void navigateToPricingPage() {
        log.info("💰 Navigating to pricing page");

        if (homePage == null) {
            openWebApplication();
        }

        pricingPage = homePage.goToPricing();

        ReportPublisher.step("Navigated to pricing page");
    }

    @When("I select premium plan")
    public void selectPremiumPlan() {
        log.info("✨ Selecting premium plan");

        assertNotNull(pricingPage, "Pricing page not loaded");
        checkoutPage = pricingPage.selectPremiumPlan();

        ReportPublisher.step("Selected premium plan");
    }

    @Then("I should see checkout page")
    public void verifyCheckoutPage() {
        log.info("✅ Verifying checkout page");

        assertNotNull(checkoutPage, "Checkout page not loaded");
        checkoutPage.assertPageLoaded();

        ReportPublisher.step("Checkout page displayed");
    }
}

