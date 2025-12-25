package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProfilePage
 *
 * Page Object for user profile page.
 *
 * Covers:
 * - Profile information
 * - Settings
 * - Subscription management
 * - Account actions
 */
public class ProfilePage extends BaseWebPage {

    // Selectors
    private static final String PAGE_HEADING = "h1:has-text('Profile')";
    private static final String USER_NAME = "[data-testid='user-name']";
    private static final String USER_EMAIL = "[data-testid='user-email']";
    private static final String USER_PHONE = "[data-testid='user-phone']";
    private static final String EDIT_PROFILE_BUTTON = "[data-testid='edit-profile']";
    private static final String SUBSCRIPTION_STATUS = "[data-testid='subscription-status']";
    private static final String MANAGE_SUBSCRIPTION_BUTTON = "[data-testid='manage-subscription']";
    private static final String LOGOUT_BUTTON = "[data-testid='logout']";

    public ProfilePage(Page page) {
        super(page);
    }

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying profile page loaded");

        actions.waitForVisible(PAGE_HEADING, 10);
        assertTrue(actions.isVisible(PAGE_HEADING), "Profile page heading not visible");

        log.info("Profile page loaded successfully");
    }

    public String getUserName() {
        return actions.getText(USER_NAME);
    }

    public String getUserEmail() {
        return actions.getText(USER_EMAIL);
    }

    public String getSubscriptionStatus() {
        return actions.getText(SUBSCRIPTION_STATUS);
    }

    public void editProfile() {
        actions.click(EDIT_PROFILE_BUTTON);
    }

    public SubscriptionSettingsPage manageSubscription() {
        actions.click(MANAGE_SUBSCRIPTION_BUTTON);
        return new SubscriptionSettingsPage(page);
    }

    public void logout() {
        actions.click(LOGOUT_BUTTON);
    }
}

