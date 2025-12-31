package com.company.qa.unified.ai;

import dev.langchain4j.model.ollama.OllamaChatModel;

/**
 * Utility to create Local LLM clients using Ollama.
 */
public final class LocalLLMUtil {

    private LocalLLMUtil() {}

    public static OllamaChatModel createOllamaModel() {
        return OllamaChatModel.builder()
                .baseUrl(
                        System.getenv().getOrDefault(
                                "OLLAMA_BASE_URL",
                                "http://localhost:11434"
                        )
                )
                .modelName("qwen2.5-coder:latest")
                .temperature(0.5)
                .build();
    }
}

