package com.example.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import tools.jackson.databind.node.JsonNodeFactory;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BankTransactionProducer {

    private final String bootstrapServer;
    private final String targetTopic;

    public BankTransactionProducer(String bootstrapServer, String targetTopic) {
        this.bootstrapServer = bootstrapServer;
        this.targetTopic = targetTopic;
    }

    public static void main(String[] args) {
        var app = new BankTransactionProducer(
                args.length >= 1 ? args[0] : "localhost:9092",
                args.length >= 2 ? args[1] : "streams-bank-transaction-input"
        );
        var producer = new KafkaProducer<String, String>(app.createProperties());
        int i = 0;
        while (true) {
            log.info("Producing batch: {}", i);
            try {
                producer.send(app.newRandomTransaction("max"));
                TimeUnit.MILLISECONDS.sleep(100);
                producer.send(app.newRandomTransaction("john"));
                TimeUnit.MILLISECONDS.sleep(100);
                producer.send(app.newRandomTransaction("alice"));
                TimeUnit.MILLISECONDS.sleep(100);
                ++i;
            } catch (InterruptedException e) {
                break;
            }
        }
        producer.close();
    }

    private Properties createProperties() {
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3"); // we don't want to lose data
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // ensure we don't push duplicates
        return properties;
    }

    private ProducerRecord<String, String> newRandomTransaction(String name) {
        var transaction = JsonNodeFactory.instance.objectNode();
        transaction.put("name", name);
        transaction.put("amount", ThreadLocalRandom.current().nextInt(0, 100));
        transaction.put("time", Instant.now().toString());
        return new ProducerRecord<>(targetTopic, name, transaction.toString());
    }
}
