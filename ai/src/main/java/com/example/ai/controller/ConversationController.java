package com.example.ai.controller;

import com.example.ai.dto.MessageDto;
import com.example.ai.service.OllamaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final OllamaService service;

    @GetMapping("/{userId}")
    public List<MessageDto> getConversationHistory(
            @PathVariable @Size(min = 1, max = 100) String userId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        return service.getConversationHistory(userId, limit);
    }

    @DeleteMapping("/{userId}")
    public void clearConversation(@PathVariable @Size(min = 1, max = 100) String userId) {
        service.clearConversation(userId);
    }
}
