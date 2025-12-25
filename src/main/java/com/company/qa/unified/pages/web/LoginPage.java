package com.company.qa.unified.pages.web;

import com.microsoft.playwright.Page;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginPage
 *
 * Page Object for the login page.
 *
 * Covers:
 * - Email/password login
 * - OTP login
 * - Social login
 * - Error handling
 */
public class LoginPage extends BaseWebPage {

    // Selectors
    private static final String EMAIL_INPUT = "[data-testid='email-input']";
    private static final String PASSWORD_INPUT = "[data-testid='password-input']";
    private static final String LOGIN_BUTTON = "[data-testid='login-button']";
    private static final String PHONE_INPUT = "[data-testid='phone-input']";
    private static final String OTP_INPUT = "[data-testid='otp-input']";
    private static final String SEND_OTP_BUTTON = "[data-testid='send-otp-button']";
    private static final String VERIFY_OTP_BUTTON = "[data-testid='verify-otp-button']";
    private static final String ERROR_MESSAGE = "[data-testid='error-message']";
    private static final String SIGNUP_LINK = "[data-testid='signup-link']";
    private static final String FORGOT_PASSWORD_LINK = "[data-testid='forgot-password-link']";

    public LoginPage(Page page) {
        super(page);
    }

    /* =========================================================
       ASSERTIONS
       ========================================================= */

    @Override
    public void assertPageLoaded() {
        log.info("✅ Verifying login page loaded");

        actions.waitForVisible(EMAIL_INPUT, 10);
        assertTrue(actions.isVisible(EMAIL_INPUT), "Email input not visible");
        assertTrue(actions.isVisible(LOGIN_BUTTON), "Login button not visible");

        log.info("Login page loaded successfully");
    }

    public void assertLoginError(String expectedError) {
        log.info("✅ Verifying login error message");

        actions.waitForVisible(ERROR_MESSAGE, 5);
        String actualError = actions.getText(ERROR_MESSAGE);

        assertTrue(
            actualError.contains(expectedError),
            "Expected error '" + expectedError + "' but got '" + actualError + "'"
        );
    }

    /* =========================================================
       ACTIONS - EMAIL/PASSWORD LOGIN
       ========================================================= */

    public LoginPage enterEmail(String email) {
        log.info("📧 Entering email");

        actions.waitForVisible(EMAIL_INPUT, 10);
        actions.type(EMAIL_INPUT, email);

        return this;
    }

    public LoginPage enterPassword(String password) {
        log.info("🔑 Entering password");

        actions.waitForVisible(PASSWORD_INPUT, 10);
        actions.type(PASSWORD_INPUT, password);

        return this;
    }

    public HomePage clickLogin() {
        log.info("🔐 Clicking login button");

        actions.click(LOGIN_BUTTON);
        actions.waitForNetworkIdle(5);

        return new HomePage(page);
    }

    public HomePage login(String email, String password) {
        log.info("🔐 Performing login");

        enterEmail(email);
        enterPassword(password);
        return clickLogin();
    }

    /* =========================================================
       ACTIONS - OTP LOGIN
       ========================================================= */

    public LoginPage enterPhoneNumber(String phoneNumber) {
        log.info("📱 Entering phone number");

        actions.waitForVisible(PHONE_INPUT, 10);
        actions.type(PHONE_INPUT, phoneNumber);

        return this;
    }

    public LoginPage requestOtp() {
        log.info("📨 Requesting OTP");

        actions.click(SEND_OTP_BUTTON);
        actions.waitForVisible(OTP_INPUT, 10);

        return this;
    }

    public LoginPage enterOtp(String otp) {
        log.info("🔢 Entering OTP");

        actions.waitForVisible(OTP_INPUT, 10);
        actions.type(OTP_INPUT, otp);

        return this;
    }

    public HomePage verifyOtp() {
        log.info("✅ Verifying OTP");

        actions.click(VERIFY_OTP_BUTTON);
        actions.waitForNetworkIdle(5);

        return new HomePage(page);
    }

    public HomePage loginWithOtp(String phoneNumber, String otp) {
        log.info("🔐 Performing OTP login");

        enterPhoneNumber(phoneNumber);
        requestOtp();
        enterOtp(otp);
        return verifyOtp();
    }

    /* =========================================================
       NAVIGATION
       ========================================================= */

    public SignupPage goToSignup() {
        log.info("📝 Navigating to signup");

        actions.click(SIGNUP_LINK);

        return new SignupPage(page);
    }

    public void goToForgotPassword() {
        log.info("🔑 Navigating to forgot password");

        actions.click(FORGOT_PASSWORD_LINK);
    }

    /* =========================================================
       GETTERS
       ========================================================= */

    public boolean isErrorDisplayed() {
        return actions.isVisible(ERROR_MESSAGE);
    }

    public String getErrorMessage() {
        return actions.getText(ERROR_MESSAGE);
    }
}

