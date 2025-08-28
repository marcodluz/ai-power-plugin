package com.marcodluz.aipowerplugin.ai;

public class AIResponse {
    private final boolean success;
    private final String content;
    private final String errorMessage;
    private final int tokensUsed;
    
    private AIResponse(boolean success, String content, String errorMessage, int tokensUsed) {
        this.success = success;
        this.content = content;
        this.errorMessage = errorMessage;
        this.tokensUsed = tokensUsed;
    }
    
    public static AIResponse success(String content, int tokensUsed) {
        return new AIResponse(true, content, null, tokensUsed);
    }
    
    public static AIResponse error(String errorMessage) {
        return new AIResponse(false, null, errorMessage, 0);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
}
