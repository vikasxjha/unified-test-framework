package com.company.qa.unified.e2e;

import com.company.qa.unified.config.MailConfig;
import com.company.qa.unified.utils.MonitoringMail;
import org.testng.annotations.Test;

/**
 * End-to-End test for MonitoringMail utility.
 * Simulates test failure alert.
 */
public class MonitoringMailE2ETest {

    @Test
    public void shouldSendMonitoringEmail() {

        MonitoringMail mail = new MonitoringMail(
                MailConfig.SMTP_HOST,
                MailConfig.SMTP_PORT,
                MailConfig.SMTP_USER,
                MailConfig.SMTP_PASSWORD
        );

        String[] recipients = {
                "qa-team@company.com",
                "devops@company.com"
        };

        String subject = "[ALERT] Smoke Test Failure - PROD";

        String body = """
                <html>
                <body>
                    <h2 style="color:red;">🚨 Automation Alert</h2>
                    <p><b>Service:</b> Authentication API</p>
                    <p><b>Environment:</b> PROD</p>
                    <p><b>Status:</b> FAILED</p>
                    <p><b>Failure:</b> Login API returned 500</p>
                    <br/>
                    <p>Please investigate immediately.</p>
                </body>
                </html>
                """;

        mail.sendMail(
                MailConfig.FROM_EMAIL,
                recipients,
                subject,
                body
        );
    }
}
