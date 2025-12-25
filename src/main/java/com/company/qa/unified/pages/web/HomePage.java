package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HomePage
 *
 * Page Object for the application home page.
 *
 * Covers:
 * - Navigation
 * - Search
 * - User state verification
 * - Main menu
 */
public class HomePage extends BaseWebPage {

    // Selectors
    private static final String LOGO = "[data-testid='logo']";
    private static final String SEARCH_INPUT = "[data-testid='search-input']";
    private static final String SEARCH_BUTTON = "[data-testid='search-button']";
    private static final String LOGIN_BUTTON = "[data-testid='login-button']";
    private static final String PROFILE_MENU = "[data-testid='profile-menu']";
    private static final String PRICING_LINK = "[data-testid='pricing-link']";
    private static final String USER_AVATAR = "[data-testid='user-avatar']";
    private static final String LOGOUT_BUTTON = "[data-testid='logout-button']";

    public HomePage(Page page) {
        super(page);
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying home page loaded");

        actions.waitForVisible(LOGO, 10);
        assertTrue(actions.isVisible(LOGO), "Logo not visible on home page");

        log.info("Home page loaded successfully");
    }

    public void assertUserLoggedIn() {
        log.info("✅ Verifying user logged in");

        actions.waitForVisible(USER_AVATAR, 10);
        assertTrue(actions.isVisible(USER_AVATAR), "User avatar not visible");

        log.info("User is logged in");
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    public SearchPage search(String query) {
        log.info("🔍 Searching for: {}", query);

        actions.waitForVisible(SEARCH_INPUT, 10);
        actions.type(SEARCH_INPUT, query);
        actions.click(SEARCH_BUTTON);

        return new SearchPage(page);
    }

    public LoginPage goToLogin() {
        log.info("🔐 Navigating to login page");

        actions.click(LOGIN_BUTTON);

        return new LoginPage(page);
    }

    public PricingPage goToPricing() {
        log.info("💰 Navigating to pricing page");

        actions.click(PRICING_LINK);

        return new PricingPage(page);
    }

    public ProfilePage goToProfile() {
        log.info("👤 Navigating to profile");

        actions.click(PROFILE_MENU);

        return new ProfilePage(page);
    }

    public void logout() {
        log.info("🚪 Logging out");

        actions.click(PROFILE_MENU);
        actions.waitForVisible(LOGOUT_BUTTON, 5);
        actions.click(LOGOUT_BUTTON);
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public boolean isUserLoggedIn() {
        return actions.isVisible(USER_AVATAR);
    }

    public String getPageHeading() {
        return actions.getText("h1");
    }
}

