package com.example.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void addDocument_shouldAddToVectorStoreAndReturnId() {
        var metadata = Map.<String, Object>of("source", "test");

        var id = documentService.addDocument("Test content", metadata);

        assertThat(id).isNotBlank();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getText()).isEqualTo("Test content");
    }

    @Test
    void delete_shouldDelegateToVectorStore() {
        documentService.delete("doc-123");

        verify(vectorStore).delete(List.of("doc-123"));
    }
}
