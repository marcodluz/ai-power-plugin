package com.marcodluz.aipowerplugin.ai;

import java.util.concurrent.CompletableFuture;

public interface AIService {
    
    /**
     * Send a message to the AI provider and get a response
     * @param message The user's message
     * @param systemPrompt The system prompt to use
     * @return CompletableFuture with the AI response
     */
    CompletableFuture<AIResponse> chat(String message, String systemPrompt);
    
    /**
     * Check if the service is properly configured
     * @return true if configured and ready to use
     */
    boolean isConfigured();
    
    /**
     * Get the provider name
     * @return provider name (e.g., "openai", "bedrock")
     */
    String getProviderName();
}
