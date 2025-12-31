package com.company.qa.unified.e2e;

import com.company.qa.unified.ai.BrowserAgent;
import com.company.qa.unified.ai.LocalLLMUtil;
import com.microsoft.playwright.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End test using Local LLM (Ollama) + Playwright.
 */
public class LocalLLMBrowserAgentE2ETest {

    @Test
    public void localLlmCanAssistBrowserAutomation() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false));

            Page page = browser.newPage();

            ChatLanguageModel llm = LocalLLMUtil.createOllamaModel();

            BrowserAgent agent = new BrowserAgent(llm, page);

            String result = agent.run("""
                Go to Google.
                Search for executeautomation GitHub page.
                Navigate to the GitHub profile.
                Find how many repositories exist.
                Return only the number.
            """);

            System.out.println("Repository Count: " + result);

            // Assertion
            assertThat(result)
                    .as("Repository count should be numeric")
                    .matches("\\d+");

            browser.close();
        }
    }
}
