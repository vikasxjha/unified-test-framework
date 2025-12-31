package com.company.qa.unified.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class OpenAiResponseParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    private OpenAiResponseParser() {}

    public static String extractLocator(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }
}
