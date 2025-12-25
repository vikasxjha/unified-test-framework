package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SignupPage
 *
 * Page Object for user signup/registration page.
 */
public class SignupPage extends BaseWebPage {

    private static final String PAGE_HEADING = "h1:has-text('Sign Up')";
    private static final String EMAIL_INPUT = "[data-testid='signup-email']";
    private static final String PASSWORD_INPUT = "[data-testid='signup-password']";
    private static final String CONFIRM_PASSWORD_INPUT = "[data-testid='signup-confirm-password']";
    private static final String SIGNUP_BUTTON = "[data-testid='signup-submit']";

    public SignupPage(Page page) {
        super(page);
    }

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying signup page loaded");

        actions.waitForVisible(PAGE_HEADING, 10);
        assertTrue(actions.isVisible(PAGE_HEADING), "Signup page heading not visible");
    }

    public SignupPage enterEmail(String email) {
        actions.type(EMAIL_INPUT, email);
        return this;
    }

    public SignupPage enterPassword(String password) {
        actions.type(PASSWORD_INPUT, password);
        return this;
    }

    public SignupPage confirmPassword(String password) {
        actions.type(CONFIRM_PASSWORD_INPUT, password);
        return this;
    }

    public HomePage submitSignup() {
        actions.click(SIGNUP_BUTTON);
        actions.waitForNetworkIdle(5);
        return new HomePage(page);
    }
}

