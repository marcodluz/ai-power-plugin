package com.marcoluz.aipowerplugin;

import com.marcoluz.aipowerplugin.ai.AIService;
import com.marcoluz.aipowerplugin.ai.AIServiceFactory;
import com.marcoluz.aipowerplugin.commands.AICommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.util.logging.Logger;

public class AIPowerPlugin extends JavaPlugin {
    
    private static AIPowerPlugin instance;
    private Logger logger;
    private AIService aiService;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Log startup message
        logger.info("AIPowerPlugin is starting up...");
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize AI service
        initializeAIService();
        
        // Register commands
        registerCommands();
        
        // Initialize bStats metrics
        new Metrics(this, 20000); // Replace with your actual bStats plugin ID
        
        logger.info("AIPowerPlugin has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        logger.info("AIPowerPlugin has been disabled.");
    }
    
    private void registerCommands() {
        // Register the main command
        getCommand("ai").setExecutor(new AICommand(this));
    }
    
    public static AIPowerPlugin getInstance() {
        return instance;
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        initializeAIService();
        logger.info("Configuration reloaded!");
    }
    
    private void initializeAIService() {
        try {
            String defaultProvider = getConfig().getString("plugin.default-provider", "openai");
            ConfigurationSection providerConfig = getConfig().getConfigurationSection("ai-providers." + defaultProvider);
            
            if (providerConfig != null) {
                aiService = AIServiceFactory.createService(defaultProvider, providerConfig);
                logger.info("AI service initialized for provider: " + defaultProvider);
            } else {
                logger.warning("No configuration found for provider: " + defaultProvider);
            }
        } catch (Exception e) {
            logger.severe("Failed to initialize AI service: " + e.getMessage());
        }
    }
    
    public AIService getAIService() {
        return aiService;
    }
}
