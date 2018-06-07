package com.coloza.sample;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public Runner(RabbitTemplate rabbitTemplate, Receiver receiver) {
        this.rabbitTemplate = rabbitTemplate;
        this.receiver = receiver;
    }

    @Override
    public void run(String... args) throws Exception {
        // rpc: request/reply pattern
        for (int i = 1; i <= 5; i++) {
            System.out.println("Sending message [" + i + "] ...");
            RelyObjectMessage relyObjectMessage = (RelyObjectMessage) rabbitTemplate.convertSendAndReceive(
                    Application.TOPIC_EXCHANGE_NAME, "foo.bar.baz",
                    new SendObjectMessage(i, "Hello message from RabbitMQ"));
            System.out.println("Received rely message <" + relyObjectMessage + ">");
            receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
        }
    }

}
