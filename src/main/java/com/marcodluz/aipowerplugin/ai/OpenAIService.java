package com.marcodluz.aipowerplugin.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.*;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OpenAIService implements AIService {
    
    private final OkHttpClient client;
    private final Gson gson;
    private final ConfigurationSection config;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int maxTokens;
    private final double temperature;
    
    public OpenAIService(ConfigurationSection config) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.config = config;
        this.apiKey = config.getString("api-key", "");
        this.model = config.getString("model", "gpt-3.5-turbo");
        this.baseUrl = config.getString("base-url", "https://api.openai.com/v1");
        this.maxTokens = config.getInt("max-tokens", 150);
        this.temperature = config.getDouble("temperature", 0.7);
    }
    
    @Override
    public CompletableFuture<AIResponse> chat(String message, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", model);
                requestBody.addProperty("max_tokens", maxTokens);
                requestBody.addProperty("temperature", temperature);
                
                JsonArray messages = new JsonArray();
                
                // Add system message
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", systemPrompt);
                messages.add(systemMessage);
                
                // Add user message
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", message);
                messages.add(userMessage);
                
                requestBody.add("messages", messages);
                
                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );
                
                Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        return AIResponse.error("OpenAI API error: " + response.code() + " " + response.message());
                    }
                    
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    
                    String content = jsonResponse
                        .getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();
                    
                    int tokensUsed = jsonResponse
                        .getAsJsonObject("usage")
                        .get("total_tokens")
                        .getAsInt();
                    
                    return AIResponse.success(content.trim(), tokensUsed);
                }
                
            } catch (IOException e) {
                return AIResponse.error("Network error: " + e.getMessage());
            } catch (Exception e) {
                return AIResponse.error("Parsing error: " + e.getMessage());
            }
        });
    }
    
    @Override
    public boolean isConfigured() {
        return !apiKey.trim().isEmpty() && !apiKey.startsWith("your-");
    }
    
    @Override
    public String getProviderName() {
        return "openai";
    }
}
