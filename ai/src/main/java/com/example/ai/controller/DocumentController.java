package com.example.ai.controller;

import com.example.ai.dto.DocumentRequest;
import com.example.ai.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addDocument(@RequestBody @Valid DocumentRequest request) {
        var documentId = documentService.addDocument(request.content(), request.metadata());
        return Map.of("id", documentId, "status", "added");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable String id) {
        documentService.delete(id);
    }
}
