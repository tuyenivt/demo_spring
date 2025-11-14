package com.coloza.sample.kafka;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class ProducerAppTests {

    private final ProducerApp app = new ProducerApp("localhost:9092");

    @Test
    void produceMessage() {
        app.produceMessage("first_topic", "hello world");
    }

    @Test
    void produceMessageWithCallback() {
        app.produceMessageWithCallback("first_topic", "hello world and callback");
    }

    @Test
    void produceMessageKey() throws ExecutionException, InterruptedException {
        app.produceMessageKey("first_topic", "hello world and key");
    }

    @Test
    void produceMessageWithSafeProducer() {
        app.produceMessageWithSafeProducer("first_topic", "hello world, i'm safe");
    }

    @Test
    void produceMessageWithHighThroughputProducer() {
        app.produceMessageWithHighThroughputProducer("first_topic", "hello world, i'm fast");
    }
}
