# demo_ai

## Overview
This is a demo AI-powered customer support chatbot designed to assist users with inquiries about **HealthConnect**, a telehealth platform. 

## Features
- Integration with **Ollama**, utilizing the **LLaMA 3.1** language model
- Predefined system prompts to guide chatbot behavior and maintain consistent tone
- **Qdrant** used as a vector store for semantic search and retrieval
- Retrieval-Augmented Generation (RAG) powered by **QuestionAnswerAdvisor** for more accurate and context-aware responses

## Start Ollama
```shell
docker run -d --name ai-ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama:0.7.1
```

## Start Vector DB
```shell
docker run -d --name ai-qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant:v1.14.1
```

## Test via REST API
```shell
curl -X POST http://localhost:8080/question/test_user \
     -H "Content-Type: application/json" \
     -d '{"question": "What are insurance supported?"}'
```
