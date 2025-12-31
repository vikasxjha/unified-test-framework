package com.company.qa.unified.e2e;

import com.company.qa.unified.base.BaseWebTest;
import com.company.qa.unified.utils.AlertUtil;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test for browser alert handling.
 *
 * Scenario:
 * - Open Rediff login page
 * - Submit empty login form
 * - Capture and accept alert
 * - Validate alert message
 */
public class HandlingAlertsE2ETest extends BaseWebTest {

    @Test
    public void alertIsHandledAndMessageValidated() {

        // 1️⃣ Navigate
        page.navigate("https://mail.rediff.com/cgi-bin/login.cgi");
        assertThat(page.title()).contains("Rediffmail");

        // 2️⃣ Register alert handler BEFORE triggering alert
        AtomicReference<String> alertMessage =
                AlertUtil.acceptAlert(page);

        // 3️⃣ Trigger alert
        page.locator("[type='submit']").click();

        // 4️⃣ Validate alert message
        assertThat(alertMessage.get())
                .as("Alert message")
                .contains("Please enter a valid user name");
    }
}
