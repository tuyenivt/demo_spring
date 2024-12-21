package com.example.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenAIServiceTest {

    @Autowired
    private OpenAIService service;

    @Test
    void getAnswer() {
        var answer = service.getAnswer("What is the capital of France?");
        System.out.println("Answer: " + answer);
    }
}
