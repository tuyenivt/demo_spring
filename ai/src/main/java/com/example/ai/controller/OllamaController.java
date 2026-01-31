package com.example.ai.controller;

import com.example.ai.dto.AnswerResponse;
import com.example.ai.dto.QuestionRequest;
import com.example.ai.service.OllamaService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class OllamaController {
    private final OllamaService service;

    @PostMapping("/question/{userId}")
    @RateLimiter(name = "questionApi", fallbackMethod = "rateLimitFallback")
    public AnswerResponse question(@PathVariable String userId, @RequestBody @Valid QuestionRequest request) {
        return new AnswerResponse(service.getAnswer(userId, request.question()), userId);
    }

    @GetMapping(value = "/question/{userId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimiter(name = "questionApi", fallbackMethod = "streamRateLimitFallback")
    public Flux<String> streamQuestion(@PathVariable String userId, @RequestParam String question) {
        return service.streamAnswer(userId, question);
    }

    @SuppressWarnings("unused") // Parameters required by Resilience4j fallback signature
    public AnswerResponse rateLimitFallback(String userId, QuestionRequest request, Exception e) {
        return new AnswerResponse("Too many requests. Please try again later.", userId);
    }

    @SuppressWarnings("unused") // Parameters required by Resilience4j fallback signature
    public Flux<String> streamRateLimitFallback(String userId, String question, Exception e) {
        return Flux.just("Too many requests. Please try again later.");
    }
}
