# AI Subproject

## Overview
Spring AI-powered customer support chatbot for HealthConnect telehealth platform. Uses Ollama (LLaMA 3.1) with RAG pattern and Qdrant vector store.

## Tech Stack
- Spring Boot + Spring AI 1.1.2
- Ollama (LLaMA 3.1 model)
- Qdrant vector database
- Resilience4j (rate limiting)
- Spring Actuator (health checks, metrics)
- Micrometer (observability)
- Testcontainers (integration tests)
- Lombok

## Project Structure
```
ai/
├── src/main/java/com/example/ai/
│   ├── MainApplication.java
│   ├── config/
│   │   └── AiConfiguration.java         # ChatClient bean + system prompt
│   ├── controller/
│   │   ├── OllamaController.java        # Question endpoints
│   │   ├── ConversationController.java  # Conversation history
│   │   └── DocumentController.java      # Document management
│   ├── dto/
│   │   ├── QuestionRequest.java         # Input DTO with validation
│   │   ├── AnswerResponse.java          # Response DTO
│   │   ├── MessageDto.java              # Conversation message
│   │   ├── DocumentRequest.java         # Document input
│   │   └── ErrorResponse.java           # Error response
│   ├── exception/
│   │   ├── AiExceptionHandler.java      # Global exception handler
│   │   └── AiServiceException.java      # Custom exception
│   ├── health/
│   │   └── AiHealthIndicator.java       # Ollama health check
│   └── service/
│       ├── OllamaService.java           # Chat logic
│       ├── RagDocumentLoader.java       # Idempotent RAG document loading
│       └── DocumentService.java         # Document CRUD via VectorStore
├── src/main/resources/
│   ├── application.yml
│   ├── prompts/
│   │   └── system-prompt.st             # Externalised system prompt
│   └── docs/                            # RAG documents
│       ├── insurance-policy.txt
│       └── platform-usage.txt
├── src/test/java/com/example/ai/
│   ├── OllamaControllerIntegrationTest.java
│   ├── TestcontainersConfiguration.java
│   ├── controller/
│   │   ├── OllamaControllerTest.java         # @WebMvcTest
│   │   ├── ConversationControllerTest.java   # @WebMvcTest
│   │   └── DocumentControllerTest.java       # @WebMvcTest
│   └── service/
│       ├── OllamaServiceTest.java            # Unit test (mocked)
│       └── DocumentServiceTest.java          # Unit test (mocked)
└── build.gradle
```

## Key Components

### AiConfiguration
- Defines `ChatClient` bean with RAG and memory advisors
- Loads system prompt from `classpath:prompts/system-prompt.st`

### OllamaController
- `POST /question/{userId}` - accepts question JSON, returns AI response
- `GET /question/{userId}/stream` - SSE streaming response
- Rate limiting via Resilience4j with fallback responses
- `@Validated` with `@Size` on `userId` and `@NotBlank @Size` on stream `question` param

### ConversationController
- `GET /conversations/{userId}` - get conversation history
- `DELETE /conversations/{userId}` - clear conversation (returns 204 No Content)

### DocumentController
- `POST /admin/documents` - add documents to vector store (returns 201 Created)
- `DELETE /admin/documents/{id}` - delete document (returns 204 No Content)

### OllamaService
- Uses injected `ChatClient` for AI conversations
- Structured logging at DEBUG (question/answer length) and WARN (failures)
- Micrometer metrics: `ai.questions.total`, `ai.response.time`

### RagDocumentLoader
- `ApplicationRunner` that loads RAG documents on startup
- Idempotent: checks if documents with matching `source` metadata already exist before adding
- Logs document loading results and failures

## Configuration
```yaml
spring.ai.ollama:
  base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
  chat.options.model: ${OLLAMA_MODEL:llama3.1}

spring.ai.vectorstore.qdrant:
  host: ${QDRANT_HOST:localhost}
  port: ${QDRANT_PORT:6334}
  collection-name: telehealth_docs
  initialize-schema: true

resilience4j.ratelimiter.instances.questionApi:
  limit-for-period: 10
  limit-refresh-period: 1m

management.endpoints.web.exposure.include: health,metrics,prometheus
```

## Running Locally

### Prerequisites
```bash
# Ollama
docker run -d --name ai-ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama:0.15.2

# Qdrant
docker run -d --name ai-qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant:v1.16
```

### Test API
```bash
# Ask question
curl -X POST http://localhost:8080/question/test_user \
     -H "Content-Type: application/json" \
     -d '{"question": "What insurance providers are supported?"}'

# Stream response
curl -N http://localhost:8080/question/test_user/stream?question=Hello

# Get conversation history
curl http://localhost:8080/conversations/test_user

# Clear conversation
curl -X DELETE http://localhost:8080/conversations/test_user

# Health check
curl http://localhost:8080/actuator/health
```

## Running Tests
```bash
# All tests (integration tests require Docker)
./gradlew :ai:test

# Unit tests only (no Docker required)
./gradlew :ai:test --tests "com.example.ai.service.*" --tests "com.example.ai.controller.*Test"
```

## Spring AI Patterns Used
1. **ChatClient Builder** - fluent API for AI conversations, configured as a Spring bean
2. **Advisors** - QuestionAnswerAdvisor (RAG) + PromptChatMemoryAdvisor (memory)
3. **VectorStore** - semantic document search with Qdrant
4. **ChatMemory** - per-user conversation tracking via conversation ID
5. **Streaming** - Flux-based SSE for real-time responses
6. **Prompt Template** - system prompt externalised to `.st` resource file
