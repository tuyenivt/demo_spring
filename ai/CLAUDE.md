# AI Subproject

## Overview
Spring AI-powered customer support chatbot for HealthConnect telehealth platform. Uses Ollama (LLaMA 3.1) with RAG pattern and Qdrant vector store.

## Tech Stack
- Spring Boot + Spring AI 1.0.0
- Ollama (LLaMA 3.1 model)
- Qdrant vector database
- Lombok

## Project Structure
```
ai/
├── src/main/java/com/example/ai/
│   ├── MainApplication.java
│   ├── controller/
│   │   └── OllamaController.java    # REST endpoint
│   └── service/
│       └── OllamaService.java       # Chat logic + RAG
├── src/main/resources/
│   ├── application.yml
│   └── docs/                        # RAG documents
│       ├── insurance-policy.txt
│       └── platform-usage.txt
└── build.gradle
```

## Key Components

### OllamaController
- `POST /question/{userId}` - accepts question, returns AI response
- User ID enables per-user conversation memory

### OllamaService
- Loads RAG documents into Qdrant vector store on startup
- Uses `QuestionAnswerAdvisor` for RAG context injection
- Uses `PromptChatMemoryAdvisor` for multi-turn conversations
- System prompt defines chatbot scope (booking, platform help, insurance)

## Configuration
```yaml
spring.ai.ollama:
  base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
  chat.options.model: ${OLLAMA_MODEL:llama3.1}

spring.ai.vectorstore.qdrant:
  host: ${QDRANT_HOST:localhost}
  port: ${QDRANT_PORT:6334}
  collection-name: telehealth_docs
```

## Running Locally

### Prerequisites
```bash
# Ollama
docker run -d --name ai-ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama:0.14

# Qdrant
docker run -d --name ai-qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant:v1.16
```

### Test API
```bash
curl -X POST http://localhost:8080/question/test_user \
     -H "Content-Type: application/json" \
     -d '"What insurance providers are supported?"'
```

## Spring AI Patterns Used
1. **ChatClient Builder** - fluent API for AI conversations
2. **Advisors** - QuestionAnswerAdvisor (RAG) + PromptChatMemoryAdvisor (memory)
3. **VectorStore** - semantic document search with Qdrant
4. **ChatMemory** - per-user conversation tracking via conversation ID
