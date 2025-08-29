# MCP Host Application

This project is a Spring Boot application that acts as a host for the Multi-Cloud Platform (MCP). It integrates with Spring AI to provide chat functionalities, leveraging various AI models like Ollama and OpenAI.

## Technologies Used

*   Spring Boot 3.5.5
*   Spring AI 1.0.1
*   Maven
*   Java 24
*   Lombok

## Project Structure

*   `src/main/java/com/vijay/McpHostApplication.java`: The main entry point for the Spring Boot application.
*   `src/main/java/com/vijay/config/ChatConfig.java`: Configuration for chat-related beans.
*   `src/main/java/com/vijay/config/WebCorsConfig.java`: Configuration for CORS settings.
*   `src/main/java/com/vijay/controller/ChatController.java`: REST controller for handling chat requests.
*   `src/main/java/com/vijay/controller/ChatBoatController.java`: Another REST controller, likely for specific chat bot functionalities.
*   `src/main/resources/application.yml`: Application-specific properties and configurations.
*   `pom.xml`: Maven project object model file, defining dependencies and build process.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 24 or higher
*   Maven 3.6.3 or higher

### Building the Application

To build the application, navigate to the project root directory and run:

```bash
mvn clean install
```

### Running the Application

You can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

Alternatively, you can run the compiled JAR file:

```bash
java -jar target/mcp-host-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default.

## Configuration

The `src/main/resources/application.yml` file contains the application's configuration properties. Key configurations include:

*   **`server.port`**: The port on which the application will run (default: 8080).
*   **`logging.level`**: Configures logging levels for different packages.
*   **`spring.ai.openai`**: Configuration for integrating with OpenAI-compatible endpoints (e.g., Google Gemini).
    *   `api-key`: Your API key for the OpenAI-compatible service.
    *   `base-url`: The base URL for the OpenAI-compatible API.
    *   `chat.completions-path`: The path for chat completions.
    *   `chat.options.model`: The AI model to use (e.g., `gemini-1.5-flash`).
    *   `chat.options.tool-choice`: Strategy for tool selection (e.g., `auto`).
*   **`spring.ai.ollama`**: Configuration for integrating with local Ollama models.
    *   `base-url`: The base URL for the Ollama server (default: `http://localhost:11434`).
    *   `init.pull-model-strategy`: Strategy for pulling models (e.g., `when_missing`).
    *   `chat.options.model`: The Ollama model to use (e.g., `"qwen2.5-coder:3b"`).
    *   `chat.options.temperature`: Controls the randomness of the model's output.
    *   `chat.options.format`: The output format (e.g., `"json"`).
*   **`spring.ai.mcp.client.sse.connections.my-mcp-server.url`**: The URL for the MCP server's Server-Sent Events (SSE) endpoint.

## API Endpoints

### `ChatController`

The `ChatController` (currently commented out) is intended to provide a basic chat interface.

*   **`POST /api/chat`**:
    *   **Request Body**: `ChatRequest` (JSON object with a `message` field).
    *   **Response**: `ChatResponse` (JSON object with an `answer` field) on success, or `ErrorResponse` on failure.
    *   **Functionality**: Processes user messages and uses MCP tools (e.g., `createNote`, `listFaqs`) when relevant.

### `ChatBoatController`

The `ChatBoatController` provides a more advanced chat interface with support for different AI providers and models.

*   **`POST /api/ai/chat`**:
    *   **Request Body**: `ChatRequest` (JSON object with `message`, `provider`, and `model` fields).
        *   `message`: The user's input message.
        *   `provider`: The AI provider to use (e.g., "ollama", "gemini"). Defaults to "gemini".
        *   `model`: (Optional) The specific model to use for the selected provider.
    *   **Response**: `ChatResponse` (JSON object with `provider`, `model`, and `answer` fields).
    *   **Functionality**: Routes chat requests to the specified AI provider (Ollama or Gemini) and model, leveraging MCP tools.

## Extending and Contributing

### Adding New AI Models

To add support for new AI models, you would typically:

1.  Add the relevant Spring AI starter dependency to your `pom.xml`. For example, for a new model `xyz`:
    ```xml
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-xyz</artifactId>
    </dependency>
    ```
2.  Configure the new model in `application.yml` under `spring.ai`.
3.  If necessary, create a new `ChatClient` bean in `ChatConfig.java` for the new model, similar to how `geminiClient` and `ollamaClient` are configured.
4.  Update `ChatBoatController.java` to include the new provider in the `switch` statement and inject the new `ChatClient`.

### Adding New MCP Tools

To add new MCP tools:

1.  Implement the new tool as a Spring bean.
2.  Ensure the tool is discoverable by Spring AI.
3.  Update the system prompt in `ChatController.java` or `ChatBoatController.java` to inform the AI about the new tool's availability and how to use it.

### CORS Configuration

CORS (Cross-Origin Resource Sharing) is configured in `WebCorsConfig.java`. If you need to allow requests from additional origins, modify the `addMapping` method in this file.
