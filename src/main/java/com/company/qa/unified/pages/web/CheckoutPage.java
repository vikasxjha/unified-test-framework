package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CheckoutPage
 *
 * Page Object for the checkout/payment page.
 *
 * Covers:
 * - Payment form
 * - Order summary
 * - Payment submission
 * - Confirmation
 */
public class CheckoutPage extends BaseWebPage {

    // Selectors
    private static final String PAGE_HEADING = "h1:has-text('Checkout')";
    private static final String ORDER_SUMMARY = "[data-testid='order-summary']";
    private static final String PLAN_NAME = "[data-testid='plan-name']";
    private static final String TOTAL_AMOUNT = "[data-testid='total-amount']";
    private static final String CARD_NUMBER_INPUT = "[data-testid='card-number']";
    private static final String CARD_EXPIRY_INPUT = "[data-testid='card-expiry']";
    private static final String CARD_CVV_INPUT = "[data-testid='card-cvv']";
    private static final String BILLING_NAME_INPUT = "[data-testid='billing-name']";
    private static final String BILLING_ADDRESS_INPUT = "[data-testid='billing-address']";
    private static final String BILLING_ZIP_INPUT = "[data-testid='billing-zip']";
    private static final String COMPLETE_PURCHASE_BUTTON = "[data-testid='complete-purchase']";
    private static final String CANCEL_BUTTON = "[data-testid='cancel-checkout']";
    private static final String PAYMENT_ERROR = "[data-testid='payment-error']";
    private static final String PROCESSING_INDICATOR = "[data-testid='processing']";

    public CheckoutPage(Page page) {
        super(page);
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying checkout page loaded");

        actions.waitForVisible(PAGE_HEADING, 10);
        assertTrue(actions.isVisible(PAGE_HEADING), "Checkout page heading not visible");
        assertTrue(actions.isVisible(ORDER_SUMMARY), "Order summary not visible");

        log.info("Checkout page loaded successfully");
    }

    public void assertPlanSelected(String expectedPlan) {
        log.info("✅ Verifying plan selected: {}", expectedPlan);

        String actualPlan = actions.getText(PLAN_NAME);
        assertTrue(
            actualPlan.contains(expectedPlan),
            "Expected plan '" + expectedPlan + "' but got '" + actualPlan + "'"
        );
    }

    public void assertPaymentError(String expectedError) {
        log.info("✅ Verifying payment error");

        actions.waitForVisible(PAYMENT_ERROR, 5);
        String actualError = actions.getText(PAYMENT_ERROR);
        assertTrue(
            actualError.contains(expectedError),
            "Expected error '" + expectedError + "' but got '" + actualError + "'"
        );
    }

    /* =========================================================
       ACTIONS
       ========================================================= */

    public CheckoutPage enterCardNumber(String cardNumber) {
        log.info("💳 Entering card number");

        actions.waitForVisible(CARD_NUMBER_INPUT, 10);
        actions.type(CARD_NUMBER_INPUT, cardNumber);

        return this;
    }

    public CheckoutPage enterCardExpiry(String expiry) {
        log.info("📅 Entering card expiry");

        actions.type(CARD_EXPIRY_INPUT, expiry);

        return this;
    }

    public CheckoutPage enterCardCvv(String cvv) {
        log.info("🔢 Entering CVV");

        actions.type(CARD_CVV_INPUT, cvv);

        return this;
    }

    public CheckoutPage enterBillingName(String name) {
        log.info("👤 Entering billing name");

        actions.type(BILLING_NAME_INPUT, name);

        return this;
    }

    public CheckoutPage enterBillingAddress(String address) {
        log.info("🏠 Entering billing address");

        actions.type(BILLING_ADDRESS_INPUT, address);

        return this;
    }

    public CheckoutPage enterBillingZip(String zip) {
        log.info("📮 Entering billing ZIP");

        actions.type(BILLING_ZIP_INPUT, zip);

        return this;
    }

    public void completePurchase() {
        log.info("✅ Completing purchase");

        actions.click(COMPLETE_PURCHASE_BUTTON);

        // Wait for processing to complete
        if (actions.isVisible(PROCESSING_INDICATOR)) {
            actions.waitForHidden(PROCESSING_INDICATOR, 30);
        }
    }

    public void cancelCheckout() {
        log.info("❌ Cancelling checkout");

        actions.click(CANCEL_BUTTON);
    }

    public void fillPaymentForm(String cardNumber, String expiry, String cvv,
                                 String name, String address, String zip) {
        log.info("📝 Filling payment form");

        enterCardNumber(cardNumber);
        enterCardExpiry(expiry);
        enterCardCvv(cvv);
        enterBillingName(name);
        enterBillingAddress(address);
        enterBillingZip(zip);
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public String getSelectedPlan() {
        return actions.getText(PLAN_NAME);
    }

    public String getTotalAmount() {
        return actions.getText(TOTAL_AMOUNT);
    }

    public boolean isProcessing() {
        return actions.isVisible(PROCESSING_INDICATOR);
    }

    public boolean hasPaymentError() {
        return actions.isVisible(PAYMENT_ERROR);
    }
}

