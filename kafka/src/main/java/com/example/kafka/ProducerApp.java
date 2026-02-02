package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerApp {

    private final Logger log = LoggerFactory.getLogger(ProducerApp.class);
    private final String bootstrapServer;

    public ProducerApp(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    private Properties createProducerProperties() {
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }

    public void produceMessage(String topic, String message) {
        // create producer
        var producer = new KafkaProducer<String, String>(this.createProducerProperties());

        // create a producer record
        var producerRecord = new ProducerRecord<String, String>(topic, message);

        // send data - asynchronous (data is not send because app shutdown immediately)
        producer.send(producerRecord);

        producer.flush();
        producer.close();
    }

    public void produceMessageWithCallback(String topic, String message) {
        // create producer
        var producer = new KafkaProducer<String, String>(this.createProducerProperties());

        // create a producer record
        var producerRecord = new ProducerRecord<String, String>(topic, message);

        // send data
        producer.send(producerRecord, (recordMetadata, e) -> {
            // execute every time a record is successful sent or an exception is thrown
            if (e == null) {
                // the record was successful sent
                log.info("Received new metadata. \nTopic: {}\nPartition: {}\nOffset: {}\nTimestamp: {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), recordMetadata.timestamp());
            } else {
                log.error("Error while producing", e);
            }
        });

        producer.flush();
        producer.close();
    }

    public void produceMessageKey(String topic, String message) throws ExecutionException, InterruptedException {
        // create producer
        var producer = new KafkaProducer<String, String>(this.createProducerProperties());

        for (var i = 0; i < 10; i++) {
            var key = "id_" + i;

            // create a producer record
            var producerRecord = new ProducerRecord<>(topic, key, message + " " + i);

            log.info("Key: {}", key);

            // send data
            producer.send(producerRecord, (recordMetadata, e) -> {
                // execute every time a record is successful sent or an exception is thrown
                if (e == null) {
                    // the record was successful sent
                    log.info("Received new metadata. \nTopic: {}\nPartition: {}\nOffset: {}\nTimestamp: {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), recordMetadata.timestamp());
                } else {
                    log.error("Error while producing", e);
                }
            }).get(); // block the .send() to make it synchronous - don't do this in production!
        }

        producer.flush();
        producer.close();
    }

    public void produceMessageWithSafeProducer(String topic, String message) {
        var properties = this.createProducerProperties();
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // kafka >= 1.0

        // create producer
        var producer = new KafkaProducer<String, String>(properties);

        // create a producer record
        var producerRecord = new ProducerRecord<String, String>(topic, message);

        // send data
        producer.send(producerRecord);

        producer.flush();
        producer.close();
    }

    public void produceMessageWithHighThroughputProducer(String topic, String message) {
        var properties = this.createProducerProperties();
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name);
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20"); // 20ms
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024)); // 32KB batch size

        // create producer
        var producer = new KafkaProducer<String, String>(properties);

        // create a producer record
        var producerRecord = new ProducerRecord<String, String>(topic, message);

        // send data
        producer.send(producerRecord);

        producer.flush();
        producer.close();
    }
}
