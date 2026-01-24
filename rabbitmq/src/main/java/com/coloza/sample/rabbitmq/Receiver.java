package com.coloza.sample.rabbitmq;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(5);

    public RelyObjectMessage receiveMessage(SendObjectMessage message) {
        System.out.println("Received send message <" + message + ">");
        latch.countDown();
        return new RelyObjectMessage(message.getId(), "Rely message from RabbitMQ");
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
