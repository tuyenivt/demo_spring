package com.coloza.sample.kafka;

import org.junit.jupiter.api.Test;

class StreamsAppTests {

    private final String bootstrapServer = "localhost:9092";
    private final String applicationId = "my-streams-app";
    private final StreamsApp app = new StreamsApp(bootstrapServer, applicationId);

    @Test
    void streamTest() {
        app.stream("first_topic", "second_topic");
    }
}
