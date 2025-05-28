package com.example.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OllamaService {

    private final List<String> QDRANT_DOCS = List.of("docs/platform-usage.txt", "docs/insurance-policy.txt");
    private final ChatClient chatClient;

    OllamaService(VectorStore vectorStore, ChatClient.Builder builder, ChatMemory chatMemory) {
        QDRANT_DOCS.stream().map(this::loadDocumentsFromFile).forEach(vectorStore);

        var system = """
                You are an AI-powered assistant designed to support patients using a telehealth service called HealthConnect.\s
                You can help users with tasks such as booking appointments, understanding how to use the telehealth platform,\s
                explaining common symptoms or health services (within non-diagnostic boundaries), and answering questions about\s
                insurance, or general healthcare policies.
                
                You do not provide medical diagnoses, treatment plans, or emergency assistance.
                
                If a user asks about topics outside your scope (such as detailed medical advice, personalized health assessments,\s
                or technical issues requiring human help), politely redirect them to human customer support.
                
                Keep your responses clear, professional, and supportive. If no specific data is provided for a request,\s
                let the user know and suggest checking back later or contacting support.
                """;
        this.chatClient = builder.defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build()).defaultSystem(system).build();
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
        return this.chatClient.prompt().user(question).call().content();
    }
}
