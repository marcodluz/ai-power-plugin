package com.marcodluz.aipowerplugin.ai;

import org.bukkit.configuration.ConfigurationSection;

public class AIServiceFactory {
    
    public static AIService createService(String providerName, ConfigurationSection config) {
        switch (providerName.toLowerCase()) {
            case "openai":
            case "azure":
            case "anthropic":
                return new OpenAIService(config); // OpenAI-compatible API for all three
            case "bedrock":
                return new BedrockService(config);
            default:
                throw new IllegalArgumentException("Unsupported AI provider: " + providerName);
        }
    }
}
