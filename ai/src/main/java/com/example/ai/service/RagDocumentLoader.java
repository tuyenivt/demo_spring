package com.example.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RagDocumentLoader implements ApplicationRunner {

    private static final List<String> DOCS = List.of("docs/platform-usage.txt", "docs/insurance-policy.txt");

    private final VectorStore vectorStore;

    RagDocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var docPath : DOCS) {
            if (isAlreadyLoaded(docPath)) {
                log.info("Documents from '{}' already loaded, skipping", docPath);
                continue;
            }
            var documents = loadDocumentsFromFile(docPath);
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Loaded {} documents from '{}'", documents.size(), docPath);
            }
        }
    }

    private boolean isAlreadyLoaded(String source) {
        var results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("HealthConnect")
                        .topK(1)
                        .filterExpression("source == '" + source + "'")
                        .build());
        return !results.isEmpty();
    }

    private List<Document> loadDocumentsFromFile(String filePath) {
        var resource = new ClassPathResource(filePath);
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(line -> new Document(line, Map.of("source", filePath)))
                    .toList();
        } catch (IOException e) {
            log.error("Failed to load RAG documents from '{}': {}", filePath, e.getMessage());
            return List.of();
        }
    }
}
