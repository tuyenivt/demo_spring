package com.example.ai.controller;

import com.example.ai.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OllamaController {
    private final OllamaService service;

    @PostMapping("/question/{userId}")
    public String question(@PathVariable String userId, @RequestBody String question) {
        return service.getAnswer(userId, question);
    }
}
