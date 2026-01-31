# AI Subproject

## Overview
Spring AI-powered customer support chatbot for HealthConnect telehealth platform. Uses Ollama (LLaMA 3.1) with RAG pattern and Qdrant vector store.

## Tech Stack
- Spring Boot + Spring AI 1.0.0
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
│   ├── controller/
│   │   ├── OllamaController.java       # Question endpoints
│   │   ├── ConversationController.java # Conversation history
│   │   └── DocumentController.java     # Document management
│   ├── dto/
│   │   ├── QuestionRequest.java        # Input DTO with validation
│   │   ├── AnswerResponse.java         # Response DTO
│   │   ├── MessageDto.java             # Conversation message
│   │   ├── DocumentRequest.java        # Document input
│   │   └── ErrorResponse.java          # Error response
│   ├── exception/
│   │   ├── AiExceptionHandler.java     # Global exception handler
│   │   └── AiServiceException.java     # Custom exception
│   ├── health/
│   │   └── AiHealthIndicator.java      # Ollama health check
│   └── service/
│       └── OllamaService.java          # Chat logic + RAG
├── src/main/resources/
│   ├── application.yml
│   └── docs/                           # RAG documents
│       ├── insurance-policy.txt
│       └── platform-usage.txt
├── src/test/java/com/example/ai/
│   ├── OllamaControllerIntegrationTest.java
│   └── TestcontainersConfiguration.java
└── build.gradle
```

## Key Components

### OllamaController
- `POST /question/{userId}` - accepts question JSON, returns AI response
- `GET /question/{userId}/stream` - SSE streaming response
- Rate limiting via Resilience4j with fallback responses

### ConversationController
- `GET /conversations/{userId}` - get conversation history
- `DELETE /conversations/{userId}` - clear conversation

### DocumentController
- `POST /admin/documents` - add documents to vector store

### OllamaService
- Loads RAG documents into Qdrant vector store on startup
- Uses `QuestionAnswerAdvisor` for RAG context injection
- Uses `PromptChatMemoryAdvisor` for multi-turn conversations
- System prompt defines chatbot scope (booking, platform help, insurance)
- Micrometer metrics: `ai.questions.total`, `ai.response.time`

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

# Health check
curl http://localhost:8080/actuator/health
```

## Running Tests
```bash
./gradlew :ai:test
```
Tests use Testcontainers with Ollama and Qdrant containers.

## Spring AI Patterns Used
1. **ChatClient Builder** - fluent API for AI conversations
2. **Advisors** - QuestionAnswerAdvisor (RAG) + PromptChatMemoryAdvisor (memory)
3. **VectorStore** - semantic document search with Qdrant
4. **ChatMemory** - per-user conversation tracking via conversation ID
5. **Streaming** - Flux-based SSE for real-time responses
