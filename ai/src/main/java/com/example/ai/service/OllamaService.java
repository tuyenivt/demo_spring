package com.example.ai.service;

import com.example.ai.dto.MessageDto;
import com.example.ai.exception.AiServiceException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OllamaService {

    private static final List<String> DOCS = List.of("docs/platform-usage.txt", "docs/insurance-policy.txt");
    private static final String SYSTEM_PROMPT = """
            You are an AI-powered assistant designed to support patients using a telehealth service called HealthConnect.
            You can help users with tasks such as booking appointments, understanding how to use the telehealth platform,
            explaining common symptoms or health services (within non-diagnostic boundaries), and answering questions about
            insurance, or general healthcare policies.
            
            You do not provide medical diagnoses, treatment plans, or emergency assistance.
            
            If a user asks about topics outside your scope (such as detailed medical advice, personalized health assessments,
            or technical issues requiring human help), politely redirect them to human customer support.
            
            Keep your responses clear, professional, and supportive. If no specific data is provided for a request,\s
            let the user know and suggest checking back later or contacting support.
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final Counter questionCounter;
    private final Timer responseTimer;

    OllamaService(VectorStore vectorStore, ChatClient.Builder builder, ChatMemory chatMemory, MeterRegistry meterRegistry) {
        DOCS.stream().map(this::loadDocumentsFromFile).forEach(vectorStore::add);

        this.chatClient = builder
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        PromptChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(SYSTEM_PROMPT).build();
        this.chatMemory = chatMemory;

        this.questionCounter = Counter.builder("ai.questions.total")
                .description("Total number of questions asked")
                .register(meterRegistry);

        this.responseTimer = Timer.builder("ai.response.time")
                .description("Time taken to generate AI response")
                .register(meterRegistry);
    }

    private List<Document> loadDocumentsFromFile(String filePath) {
        var resource = new ClassPathResource(filePath);
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).map(Document::new).toList();
        } catch (IOException exception) {
            return List.of();
        }
    }

    public String getAnswer(String userId, String question) {
        questionCounter.increment();
        return responseTimer.record(() -> {
            try {
                return this.chatClient.prompt()
                        .user(question)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                        .call()
                        .content();
            } catch (Exception e) {
                throw new AiServiceException("Failed to get answer from AI service", e);
            }
        });
    }

    public Flux<String> streamAnswer(String userId, String question) {
        questionCounter.increment();
        try {
            return this.chatClient.prompt()
                    .user(question)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .stream()
                    .content();
        } catch (Exception e) {
            throw new AiServiceException("Failed to stream answer from AI service", e);
        }
    }

    public List<MessageDto> getConversationHistory(String userId, int limit) {
        return chatMemory.get(userId).stream().limit(limit)
                .map(msg -> new MessageDto(msg.getMessageType().name(), msg.getText()))
                .toList();
    }

    public void clearConversation(String userId) {
        chatMemory.clear(userId);
    }
}
