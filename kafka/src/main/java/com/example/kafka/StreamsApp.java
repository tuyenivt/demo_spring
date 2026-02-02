package com.example.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class StreamsApp {

    private final String bootstrapServer;
    private final String applicationId;

    public StreamsApp(String bootstrapServer, String applicationId) {
        this.bootstrapServer = bootstrapServer;
        this.applicationId = applicationId;
    }

    private Properties createStreamsProperties() {
        var properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.applicationId);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        return properties;
    }

    public void stream(String fromTopic, String toTopic) {
        // create a topology
        var streamsBuilder = new StreamsBuilder();

        // input topic
        var inputTopic = streamsBuilder.<String, String>stream(fromTopic);
        var filterStream = inputTopic.filter((k, v) -> v.length() > 5);
        filterStream.to(toTopic);

        // build the topology
        var kafkaStreams = new KafkaStreams(streamsBuilder.build(), this.createStreamsProperties());

        // start our streams
        kafkaStreams.start();
    }
}
