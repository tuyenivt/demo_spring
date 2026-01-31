package com.example.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;

    public String addDocument(String content, Map<String, Object> metadata) {
        var document = new Document(content, metadata);
        vectorStore.add(List.of(document));
        return document.getId();
    }

    public void delete(String documentId) {
        vectorStore.delete(List.of(documentId));
    }
}
