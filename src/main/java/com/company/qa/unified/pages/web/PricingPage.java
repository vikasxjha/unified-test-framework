package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PricingPage
 *
 * Page Object for the pricing/plans page.
 *
 * Covers:
 * - Plan selection
 * - Plan comparison
 * - Pricing details
 * - CTA buttons
 */
public class PricingPage extends BaseWebPage {

    // Selectors
    private static final String PAGE_HEADING = "h1:has-text('Pricing')";
    private static final String FREE_PLAN = "[data-testid='plan-free']";
    private static final String BASIC_PLAN = "[data-testid='plan-basic']";
    private static final String PREMIUM_PLAN = "[data-testid='plan-premium']";
    private static final String SELECT_FREE_BUTTON = "[data-testid='select-free']";
    private static final String SELECT_BASIC_BUTTON = "[data-testid='select-basic']";
    private static final String SELECT_PREMIUM_BUTTON = "[data-testid='select-premium']";
    private static final String PLAN_FEATURES = "[data-testid='plan-features']";
    private static final String MONTHLY_TOGGLE = "[data-testid='billing-monthly']";
    private static final String YEARLY_TOGGLE = "[data-testid='billing-yearly']";

    public PricingPage(Page page) {
        super(page);
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying pricing page loaded");

        actions.waitForVisible(PAGE_HEADING, 10);
        assertTrue(actions.isVisible(PAGE_HEADING), "Pricing page heading not visible");

        log.info("Pricing page loaded successfully");
    }

    public void assertPlanVisible(String planName) {
        log.info("✅ Verifying plan visible: {}", planName);

        String selector = String.format("[data-testid='plan-%s']", planName.toLowerCase());
        assertTrue(actions.isVisible(selector), planName + " plan not visible");
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    public CheckoutPage selectFreePlan() {
        log.info("🆓 Selecting free plan");

        actions.click(SELECT_FREE_BUTTON);
        actions.waitForNetworkIdle(3);

        return new CheckoutPage(page);
    }

    public CheckoutPage selectBasicPlan() {
        log.info("💵 Selecting basic plan");

        actions.click(SELECT_BASIC_BUTTON);
        actions.waitForNetworkIdle(3);

        return new CheckoutPage(page);
    }

    public CheckoutPage selectPremiumPlan() {
        log.info("✨ Selecting premium plan");

        actions.click(SELECT_PREMIUM_BUTTON);
        actions.waitForNetworkIdle(3);

        return new CheckoutPage(page);
    }

    public PricingPage switchToMonthlyBilling() {
        log.info("📅 Switching to monthly billing");

        actions.click(MONTHLY_TOGGLE);
        actions.waitForNetworkIdle(2);

        return this;
    }

    public PricingPage switchToYearlyBilling() {
        log.info("📅 Switching to yearly billing");

        actions.click(YEARLY_TOGGLE);
        actions.waitForNetworkIdle(2);

        return this;
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public String getFreePlanPrice() {
        return actions.getText(FREE_PLAN + " [data-testid='price']");
    }

    public String getBasicPlanPrice() {
        return actions.getText(BASIC_PLAN + " [data-testid='price']");
    }

    public String getPremiumPlanPrice() {
        return actions.getText(PREMIUM_PLAN + " [data-testid='price']");
    }

    public boolean isMonthlyBillingSelected() {
        String monthlyClass = page.locator(MONTHLY_TOGGLE).getAttribute("class");
        return monthlyClass != null && monthlyClass.contains("active");
    }

    public boolean isYearlyBillingSelected() {
        String yearlyClass = page.locator(YEARLY_TOGGLE).getAttribute("class");
        return yearlyClass != null && yearlyClass.contains("active");
    }
}

