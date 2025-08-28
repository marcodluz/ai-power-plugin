# AI Power Plugin

A modern PaperMC Minecraft plugin template with Gradle build system.

## Features

- Modern Gradle build with Kotlin DSL
- PaperMC API integration
- Example command system with tab completion
- Configuration file support
- Permission system
- bStats metrics integration
- Ready for Java 17+

## Building

### Prerequisites

- Java 17 or higher
- Git

### Build Steps

1. Clone the repository:

   ```bash
   git clone <your-repo-url>
   cd ai-power-plugin
   ```

2. Build the plugin:

   ```bash
   ./gradlew build
   ```

3. The built plugin JAR will be in `build/libs/ai-power-plugin-1.0.0.jar`

## Installation

1. Download the latest release or build from source
2. Place the JAR file in your server's `plugins/` directory
3. Start or restart your server
4. The plugin will create a `config.yml` file in `plugins/AIPowerPlugin/`

## Commands

- `/ai help` - Show available commands
- `/ai info` - Display plugin information
- `/ai <message>` - Chat with AI assistant
- `/ai config` - Show current configuration (admin only)
- `/ai reload` - Reload configuration (admin only)

## Permissions

- `ai.use` - Basic plugin usage (default: true)
- `ai.admin` - Administrative commands (default: op)

## Configuration

The plugin creates a comprehensive `config.yml` file with support for multiple AI providers:

### AI Provider Setup

Configure one or more AI providers:

```yaml
# Plugin settings
plugin:
  default-provider: "openai" # openai, azure, anthropic, bedrock
  max-response-length: 500
  enable-chat-history: true

# AI Provider Configurations
ai-providers:
  # OpenAI
  openai:
    api-key: "your-openai-api-key-here"
    model: "gpt-3.5-turbo"
    max-tokens: 150
    temperature: 0.7

  # Azure OpenAI
  azure:
    api-key: "your-azure-openai-api-key-here"
    endpoint: "https://your-resource.openai.azure.com"
    deployment-name: "your-deployment-name"
    api-version: "2024-02-15-preview"

  # Anthropic (Claude)
  anthropic:
    api-key: "your-anthropic-api-key-here"
    model: "claude-3-haiku-20240307"

  # AWS Bedrock
  bedrock:
    region: "us-east-1"
    access-key-id: "your-aws-access-key-id"
    secret-access-key: "your-aws-secret-access-key"
    model-id: "anthropic.claude-3-haiku-20240307-v1:0"

# Rate limiting and other settings
rate-limiting:
  enabled: true
  requests-per-minute: 5
  cooldown-seconds: 10
```

### Setup Steps for AI Providers

**OpenAI:**

1. Get API key from https://platform.openai.com/api-keys
2. Set `plugin.default-provider: "openai"`
3. Set your API key in `ai-providers.openai.api-key`

**Azure OpenAI:**

1. Create Azure OpenAI resource in Azure Portal
2. Deploy a model (like GPT-3.5 or GPT-4)
3. Set `plugin.default-provider: "azure"`
4. Configure endpoint, deployment name, and API key

**Anthropic:**

1. Get API key from https://console.anthropic.com/
2. Set `plugin.default-provider: "anthropic"`
3. Configure your API key and preferred Claude model

**AWS Bedrock:**

1. Enable Bedrock in AWS Console
2. Request model access for Anthropic Claude
3. Set `plugin.default-provider: "bedrock"`
4. Configure AWS credentials and region

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/marcoluz/aipowerplugin/
│   │   ├── AIPowerPlugin.java          # Main plugin class
│   │   └── commands/
│   │       └── AICommand.java          # AI command handler
│   └── resources/
│       ├── plugin.yml                  # Plugin metadata
│       └── config.yml                  # Default configuration
├── build.gradle.kts                    # Build configuration
└── README.md                           # This file
```

### Adding New Features

1. Create new classes in appropriate packages
2. Register commands in `AIPowerPlugin.java`
3. Add command definitions to `plugin.yml`
4. Update permissions as needed

### Testing

Run the plugin on a test server:

1. Build the plugin: `./gradlew build`
2. Copy to test server: `cp build/libs/*.jar /path/to/test-server/plugins/`
3. Start the server and test functionality

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please open an issue on the GitHub repository or contact the maintainer.
