package com.marcoluz.aipowerplugin.commands;

import com.marcoluz.aipowerplugin.AIPowerPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AICommand implements CommandExecutor, TabCompleter {
    
    private final AIPowerPlugin plugin;
    
    public AICommand(AIPowerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        // Check if first argument is a subcommand
        String firstArg = args[0].toLowerCase();
        if (firstArg.equals("help") || firstArg.equals("reload") || firstArg.equals("info") || firstArg.equals("config")) {
            switch (firstArg) {
                case "help":
                    showHelp(sender);
                    break;
                    
                case "reload":
                    if (!sender.hasPermission("ai.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to reload the plugin!", NamedTextColor.RED));
                        return true;
                    }
                    plugin.reloadPluginConfig();
                    sender.sendMessage(Component.text("Plugin configuration reloaded!", NamedTextColor.GREEN));
                    break;
                    
                case "info":
                    showInfo(sender);
                    break;
                    
                case "config":
                    if (!sender.hasPermission("ai.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to view configuration!", NamedTextColor.RED));
                        return true;
                    }
                    showConfig(sender);
                    break;
            }
        } else {
            // Treat entire argument list as AI prompt
            String message = String.join(" ", args);
            handleChatRequest(sender, message);
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== AI Plugin Help ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/ai help", NamedTextColor.YELLOW).append(Component.text(" - Show this help message", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/ai info", NamedTextColor.YELLOW).append(Component.text(" - Show plugin information", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/ai <message>", NamedTextColor.YELLOW).append(Component.text(" - Chat with AI assistant", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Example: /ai What is redstone?", NamedTextColor.GRAY));
        
        if (sender.hasPermission("ai.admin")) {
            sender.sendMessage(Component.text("/ai config", NamedTextColor.YELLOW).append(Component.text(" - Show current configuration", NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/ai reload", NamedTextColor.YELLOW).append(Component.text(" - Reload plugin configuration", NamedTextColor.WHITE)));
        }
    }
    
    private void showInfo(CommandSender sender) {
        sender.sendMessage(Component.text("=== AI Plugin Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.AQUA).append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.AQUA).append(Component.text(plugin.getDescription().getAuthors().get(0), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Description: ", NamedTextColor.AQUA).append(Component.text(plugin.getDescription().getDescription(), NamedTextColor.WHITE)));
        
        // Show current AI provider
        String defaultProvider = plugin.getConfig().getString("plugin.default-provider", "openai");
        String apiKey = plugin.getConfig().getString("ai-providers." + defaultProvider + ".api-key", "");
        boolean isConfigured = !apiKey.isEmpty() && !apiKey.startsWith("your-");
        
        Component providerStatus = isConfigured ? 
            Component.text(" (Configured)", NamedTextColor.GREEN) : 
            Component.text(" (Not Configured)", NamedTextColor.RED);
        
        sender.sendMessage(Component.text("AI Provider: ", NamedTextColor.AQUA)
            .append(Component.text(defaultProvider, NamedTextColor.WHITE))
            .append(providerStatus));
    }
    
    private void showConfig(CommandSender sender) {
        sender.sendMessage(Component.text("=== AI Configuration ===", NamedTextColor.GOLD));
        
        String defaultProvider = plugin.getConfig().getString("plugin.default-provider", "openai");
        sender.sendMessage(Component.text("Current Provider: ", NamedTextColor.AQUA).append(Component.text(defaultProvider, NamedTextColor.WHITE)));
        
        // Check provider configuration status
        String apiKey = plugin.getConfig().getString("ai-providers." + defaultProvider + ".api-key", "");
        boolean isConfigured = !apiKey.isEmpty() && !apiKey.startsWith("your-");
        
        Component status = isConfigured ? 
            Component.text("Configured", NamedTextColor.GREEN) : 
            Component.text("Not Configured", NamedTextColor.RED);
        sender.sendMessage(Component.text("Provider Status: ", NamedTextColor.AQUA).append(status));
        
        // Show available providers
        sender.sendMessage(Component.text("Available Providers: ", NamedTextColor.AQUA).append(Component.text("openai, azure, anthropic, bedrock", NamedTextColor.WHITE)));
        
        boolean rateLimitEnabled = plugin.getConfig().getBoolean("rate-limiting.enabled", true);
        int requestsPerMinute = plugin.getConfig().getInt("rate-limiting.requests-per-minute", 5);
        
        String rateLimitText = rateLimitEnabled ? "Enabled (" + requestsPerMinute + " req/min)" : "Disabled";
        sender.sendMessage(Component.text("Rate Limiting: ", NamedTextColor.AQUA).append(Component.text(rateLimitText, NamedTextColor.WHITE)));
        
        int maxTokens = plugin.getConfig().getInt("ai-providers." + defaultProvider + ".max-tokens", 150);
        sender.sendMessage(Component.text("Max Tokens: ", NamedTextColor.AQUA).append(Component.text(String.valueOf(maxTokens), NamedTextColor.WHITE)));
    }
    
    private void handleChatRequest(CommandSender sender, String message) {
        sender.sendMessage(Component.text("[AI is thinking...]", NamedTextColor.GRAY));
        
        // Get AI service
        var aiService = plugin.getAIService();
        if (aiService == null || !aiService.isConfigured()) {
            sender.sendMessage(Component.text("AI provider is not properly configured. Please contact an administrator.", NamedTextColor.RED));
            return;
        }
        
        // Get system prompt from config
        String systemPrompt = plugin.getConfig().getString("messages.system-prompt", 
            "You are a helpful assistant in a Minecraft server. Keep responses concise and family-friendly.");
        
        // Call AI service asynchronously
        aiService.chat(message, systemPrompt).thenAccept(response -> {
            if (response.isSuccess()) {
                // Get prefix from config
                String prefix = plugin.getConfig().getString("plugin.prefix", "&b[AI]&r ");
                
                // Limit response length
                int maxLength = plugin.getConfig().getInt("plugin.max-response-length", 500);
                String content = response.getContent();
                if (content.length() > maxLength) {
                    content = content.substring(0, maxLength) + "...";
                }
                
                // Send AI response
                sender.sendMessage(Component.text("[AI] ", NamedTextColor.AQUA)
                    .append(Component.text(content, NamedTextColor.WHITE)));
                
                // Debug info
                if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                    sender.sendMessage(Component.text("DEBUG - Tokens used: " + response.getTokensUsed(), NamedTextColor.YELLOW));
                }
            } else {
                sender.sendMessage(Component.text("[AI] Error: " + response.getErrorMessage(), NamedTextColor.RED));
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(Component.text("[AI] Unexpected error occurred.", NamedTextColor.RED));
            plugin.getLogger().severe("AI request failed: " + throwable.getMessage());
            return null;
        });
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("help", "info"));
            if (sender.hasPermission("ai.admin")) {
                subcommands.add("config");
                subcommands.add("reload");
            }
            
            String partial = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partial)) {
                    completions.add(subcommand);
                }
            }
            
            // Also suggest common AI prompts if not a subcommand
            if (!subcommands.contains(partial)) {
                completions.add("Hello!");
                completions.add("What");
                completions.add("How");
                completions.add("Can");
            }
        }
        
        return completions;
    }
}
