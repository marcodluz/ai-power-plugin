package com.marcoluz.aipowerplugin.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.concurrent.CompletableFuture;

public class BedrockService implements AIService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final Gson gson;
    private final ConfigurationSection config;
    private final String modelId;
    private final int maxTokens;
    private final double temperature;
    private final String accessKeyId;
    private final String secretAccessKey;
    
    public BedrockService(ConfigurationSection config) {
        this.gson = new Gson();
        this.config = config;
        this.accessKeyId = config.getString("access-key-id", "");
        this.secretAccessKey = config.getString("secret-access-key", "");
        this.modelId = config.getString("model-id", "anthropic.claude-3-haiku-20240307-v1:0");
        this.maxTokens = config.getInt("max-tokens", 150);
        this.temperature = config.getDouble("temperature", 0.7);
        
        String region = config.getString("region", "us-east-1");
        
        if (isConfigured()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            
            this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        } else {
            this.bedrockClient = null;
        }
    }
    
    @Override
    public CompletableFuture<AIResponse> chat(String message, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            if (bedrockClient == null) {
                return AIResponse.error("Bedrock client not configured");
            }
            
            try {
                // Create request body for Anthropic Claude on Bedrock
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("anthropic_version", "bedrock-2023-05-31");
                requestBody.addProperty("max_tokens", maxTokens);
                requestBody.addProperty("temperature", temperature);
                requestBody.addProperty("system", systemPrompt);
                
                // Create messages array
                com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", message);
                messages.add(userMessage);
                
                requestBody.add("messages", messages);
                
                String jsonString = gson.toJson(requestBody);
                
                InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(jsonString))
                    .build();
                
                InvokeModelResponse response = bedrockClient.invokeModel(request);
                
                String responseBody = response.body().asUtf8String();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // Parse Claude response format
                String content = "";
                if (jsonResponse.has("content") && jsonResponse.getAsJsonArray("content").size() > 0) {
                    content = jsonResponse.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
                } else if (jsonResponse.has("completion")) {
                    content = jsonResponse.get("completion").getAsString();
                }
                
                // Extract token usage if available
                int tokensUsed = 0;
                if (jsonResponse.has("usage")) {
                    JsonObject usage = jsonResponse.getAsJsonObject("usage");
                    if (usage.has("output_tokens")) {
                        tokensUsed = usage.get("output_tokens").getAsInt();
                    }
                }
                
                return AIResponse.success(content.trim(), tokensUsed);
                
            } catch (Exception e) {
                return AIResponse.error("Bedrock error: " + e.getMessage());
            }
        });
    }
    
    @Override
    public boolean isConfigured() {
        return !accessKeyId.trim().isEmpty() && !accessKeyId.startsWith("your-") &&
               !secretAccessKey.trim().isEmpty() && !secretAccessKey.startsWith("your-");
    }
    
    @Override
    public String getProviderName() {
        return "bedrock";
    }
}
