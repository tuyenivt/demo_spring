package com.coloza.sample.rabbitmq.producer;

import com.coloza.sample.rabbitmq.config.RabbitMQConfig;
import com.coloza.sample.rabbitmq.dto.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final RabbitTemplate rabbitTemplate;

    public void submitPayment(Payment payment) {
        log.info("Submitting payment for processing: {}", payment);
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_QUEUE, payment);
    }
}
