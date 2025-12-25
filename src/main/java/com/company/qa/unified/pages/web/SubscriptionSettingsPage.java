package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SubscriptionSettingsPage
 *
 * Page Object for subscription management page.
 */
public class SubscriptionSettingsPage extends BaseWebPage {

    private static final String PAGE_HEADING = "h1:has-text('Subscription')";
    private static final String PLAN_NAME = "[data-testid='current-plan']";
    private static final String CANCEL_BUTTON = "[data-testid='cancel-subscription']";

    public SubscriptionSettingsPage(Page page) {
        super(page);
    }

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying subscription settings page loaded");

        actions.waitForVisible(PAGE_HEADING, 10);
        assertTrue(actions.isVisible(PAGE_HEADING), "Subscription page heading not visible");

        log.info("Subscription settings page loaded successfully");
    }

    public String getCurrentPlan() {
        return actions.getText(PLAN_NAME);
    }

    public void cancelSubscription() {
        actions.click(CANCEL_BUTTON);
    }
}

