package com.example.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordCountStreamsAppTests {

    private static final String APPLICATION_ID = "test";
    private static final String BOOTSTRAP_SERVERS = "dummy:1234";
    private static final String INPUT_TOPIC = "streams-plaintext-input";
    private static final String OUTPUT_TOPIC = "streams-wordcount-output";

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    @BeforeEach
    void setup() {
        var config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        var app = new WordCountStreamsApp();
        testDriver = new TopologyTestDriver(app.createTopology(INPUT_TOPIC, OUTPUT_TOPIC), config);
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.Long().deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void testCount() {
        inputTopic.pipeInput("kafka testing kafka streams");

        var output1 = outputTopic.readRecord();
        var output2 = outputTopic.readRecord();
        var output3 = outputTopic.readRecord();
        var output4 = outputTopic.readRecord();

        assertEquals("kafka", output1.key());
        assertEquals(1L, output1.value());

        assertEquals("testing", output2.key());
        assertEquals(1L, output2.value());

        assertEquals("kafka", output3.key());
        assertEquals(2L, output3.value());

        assertEquals("streams", output4.key());
        assertEquals(1L, output4.value());

        Assertions.assertTrue(outputTopic.isEmpty());
    }
}
