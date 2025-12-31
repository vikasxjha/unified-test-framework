package com.company.qa.unified.utils;

import com.microsoft.playwright.Page;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * AI-powered self-healing locator utility.
 *
 * Responsibilities:
 * - Detect broken locators
 * - Send page source + intent to AI
 * - Receive healed locator suggestion
 * - Retry action safely
 *
 * NOTE:
 * - This does NOT auto-change code
 * - It retries execution and logs suggestion
 */
public final class AiSelfHealingLocatorUtil {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private AiSelfHealingLocatorUtil() {}

    /**
     * Attempts to locate element using AI healing if primary locator fails.
     */
    public static String healLocatorIfNeeded(
            Page page,
            String failingLocator,
            String elementIntent
    ) {

        try {
            // Try normal locator first
            if (page.locator(failingLocator).count() > 0) {
                return failingLocator;
            }
        } catch (Exception ignored) {}

        // Capture page source
        String pageSource = page.content();

        // Ask AI for healed locator
        return askAiForHealedLocator(
                failingLocator,
                elementIntent,
                pageSource
        );
    }

    /**
     * Calls OpenAI to suggest a healed locator.
     */
    private static String askAiForHealedLocator(
            String oldLocator,
            String intent,
            String html
    ) {

        String prompt = """
            You are an expert QA automation engineer.
            
            A locator is broken.
            
            OLD LOCATOR:
            %s
            
            ELEMENT INTENT:
            %s
            
            HTML SOURCE:
            %s
            
            TASK:
            Return ONLY a Playwright-compatible CSS or text selector that best matches the intent.
            Do NOT explain.
            """.formatted(oldLocator, intent, html);

        try {
            String requestBody = """
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role": "user", "content": %s}
                  ]
                }
                """.formatted(JsonUtils.escapeJson(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_ENDPOINT))
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return OpenAiResponseParser.extractLocator(response.body());

        } catch (Exception e) {
            throw new RuntimeException("AI self-healing failed", e);
        }
    }
}
