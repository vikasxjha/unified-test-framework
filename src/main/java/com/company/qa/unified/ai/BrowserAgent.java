package com.company.qa.unified.ai;

import com.microsoft.playwright.Page;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Very simple browser agent:
 * - Uses LLM for reasoning
 * - Uses Playwright for execution
 */
public class BrowserAgent {

    private final ChatLanguageModel llm;
    private final Page page;

    public BrowserAgent(ChatLanguageModel llm, Page page) {
        this.llm = llm;
        this.page = page;
    }

    /**
     * Executes a task using LLM-guided reasoning.
     */
    public String run(String taskPrompt) {

        // Step 1: Ask LLM what to do
        String plan = llm.generate("""
            You are a browser automation agent.
            Given the task below, respond with a short plan.
            
            TASK:
            %s
            """.formatted(taskPrompt));

        System.out.println("LLM Plan:\n" + plan);

        // Step 2: Execute deterministic steps
        page.navigate("https://www.google.com");
        page.locator("input[name='q']").fill("executeautomation github");
        page.keyboard().press("Enter");

        page.locator("a:has-text('github.com/executeautomation')")
                .first()
                .click();

        // Step 3: Extract information
        String repoCountText = page.locator("a[href$='?tab=repositories'] span")
                .first()
                .innerText();

        return repoCountText;
    }
}

