package com.coloza.sample.kafka;

import org.junit.jupiter.api.Test;

import java.util.List;

class ConsumerAppTests {

    private final ConsumerApp app = new ConsumerApp("localhost:9092", "my-first-app");

    @Test
    void consumeMessage() {
        app.consumeMessage(List.of("first_topic"));
    }

    @Test
    void consumeMessageWithThread() {
        app.consumeMessageWithThread(List.of("first_topic"));
    }

    @Test
    void consumeMessageAssignAndSeek() {
        app.consumeMessageAssignAndSeek("first_topic", 10, 5);
    }

    @Test
    void consumeMessageWithManualCommit() {
        app.consumeMessageWithManualCommit(List.of("first_topic"));
    }
}
