package com.example.ai.service;

import com.example.ai.exception.AiServiceException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class OllamaServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatMemory chatMemory;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec callResponse;
    @Mock
    private ChatClient.StreamResponseSpec streamResponse;

    private MeterRegistry meterRegistry;
    private OllamaService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new OllamaService(chatClient, chatMemory, meterRegistry);
    }

    @Test
    void getAnswer_shouldReturnResponseAndIncrementCounter() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponse);
        when(callResponse.content()).thenReturn("AI response");

        var result = service.getAnswer("user1", "Hello");

        assertThat(result).isEqualTo("AI response");
        assertThat(meterRegistry.counter("ai.questions.total").count()).isEqualTo(1.0);
    }

    @Test
    void getAnswer_shouldThrowAiServiceExceptionOnFailure() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Connection failed"));

        assertThatThrownBy(() -> service.getAnswer("user1", "Hello"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Failed to get answer from AI service");
    }

    @Test
    void streamAnswer_shouldReturnFluxAndIncrementCounter() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamResponse);
        when(streamResponse.content()).thenReturn(Flux.just("chunk1", "chunk2"));

        var result = service.streamAnswer("user1", "Hello");

        StepVerifier.create(result)
                .expectNext("chunk1")
                .expectNext("chunk2")
                .verifyComplete();
        assertThat(meterRegistry.counter("ai.questions.total").count()).isEqualTo(1.0);
    }

    @Test
    void streamAnswer_shouldThrowAiServiceExceptionOnFailure() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Connection failed"));

        assertThatThrownBy(() -> service.streamAnswer("user1", "Hello"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("Failed to stream answer");
    }

    @Test
    void getConversationHistory_shouldReturnLimitedMessages() {
        List<Message> messages = List.of(
                new AssistantMessage("msg1"),
                new AssistantMessage("msg2"),
                new AssistantMessage("msg3"));
        when(chatMemory.get("user1")).thenReturn(messages);

        var result = service.getConversationHistory("user1", 2);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().content()).isEqualTo("msg1");
    }

    @Test
    void clearConversation_shouldDelegateToChatMemory() {
        service.clearConversation("user1");

        verify(chatMemory).clear("user1");
    }
}
