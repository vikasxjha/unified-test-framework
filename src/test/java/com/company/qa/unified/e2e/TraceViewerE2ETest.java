package com.company.qa.unified.e2e;

import com.company.qa.unified.utils.TraceUtil;
import com.microsoft.playwright.*;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test demonstrating Playwright Trace Viewer usage.
 *
 * Scenario:
 * - Start tracing
 * - Perform user actions
 * - Stop tracing
 * - Export trace.zip for debugging
 */
public class TraceViewerE2ETest {

    @Test
    public void traceCanBeCapturedForUserFlow() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );

            BrowserContext context = browser.newContext();

            // 1️⃣ Start tracing
            TraceUtil.startTracing(context);

            Page page = context.newPage();

            // 2️⃣ Navigate
            page.navigate("https://gmail.com");
            assertThat(page.title()).contains("Gmail");

            // 3️⃣ Perform actions
            page.locator("[type=email]").fill("trainer@way2automation.com");
            page.locator("button:has-text('Next')").click();

            page.locator("[type=password]").fill("invalidPassword");
            page.locator("button:has-text('Next')").click();

            // 4️⃣ Stop tracing and save trace
            TraceUtil.stopTracing(
                    context,
                    Paths.get("target/traces/gmail-login-trace.zip")
            );

            page.close();
            context.close();
            browser.close();
        }
    }
}
