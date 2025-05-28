package com.example.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OllamaServiceTest {

    @Autowired
    OllamaService service;

    @Test
    void getAnswerForInsurancePolicy() {
        var answer = service.getAnswer("test_user", "What are insurance supported?");
        System.out.println("Answer: " + answer);
    }

    @Test
    void getAnswerForPlatformUsage() {
        var answer = service.getAnswer("test_user", "How to book an appointment?");
        System.out.println("Answer: " + answer);
    }
}
