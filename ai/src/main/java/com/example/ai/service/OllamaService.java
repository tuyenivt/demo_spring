package com.example.ai.service;

import com.example.ai.dto.MessageDto;
import com.example.ai.exception.AiServiceException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class OllamaService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final Counter questionCounter;
    private final Timer responseTimer;

    OllamaService(ChatClient chatClient, ChatMemory chatMemory, MeterRegistry meterRegistry) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;

        this.questionCounter = Counter.builder("ai.questions.total")
                .description("Total number of questions asked")
                .register(meterRegistry);

        this.responseTimer = Timer.builder("ai.response.time")
                .description("Time taken to generate AI response")
                .register(meterRegistry);
    }

    public String getAnswer(String userId, String question) {
        questionCounter.increment();
        log.debug("Processing question for userId={}, questionLength={}", userId, question.length());
        return responseTimer.record(() -> {
            try {
                var answer = this.chatClient.prompt()
                        .user(question)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                        .call()
                        .content();
                log.debug("Generated answer for userId={}, answerLength={}", userId, answer != null ? answer.length() : 0);
                return answer;
            } catch (Exception e) {
                log.warn("Failed to get answer from AI service for userId={}: {}", userId, e.getMessage());
                throw new AiServiceException("Failed to get answer from AI service", e);
            }
        });
    }

    public Flux<String> streamAnswer(String userId, String question) {
        questionCounter.increment();
        log.debug("Streaming answer for userId={}, questionLength={}", userId, question.length());
        try {
            return this.chatClient.prompt()
                    .user(question)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .stream()
                    .content();
        } catch (Exception e) {
            log.warn("Failed to stream answer from AI service for userId={}: {}", userId, e.getMessage());
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
