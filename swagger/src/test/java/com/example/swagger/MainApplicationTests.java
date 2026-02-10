package com.example.swagger;

import io.swagger.petstore.api.PetApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MainApplicationTests {

    @Autowired
    private PetApi petApi;

    @Test
    void contextLoads() {
        assertThat(petApi).isNotNull();
    }
}
