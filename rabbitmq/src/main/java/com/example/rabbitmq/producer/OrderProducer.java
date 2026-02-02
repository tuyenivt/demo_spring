package com.example.rabbitmq.producer;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.dto.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrder(Order order) {
        var routingKey = order.isUrgent() ? RabbitMQConfig.ROUTING_KEY_HIGH : RabbitMQConfig.ROUTING_KEY_NORMAL;

        log.info("Sending order with priority '{}': {}", routingKey, order);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_DIRECT_EXCHANGE, routingKey, order);
    }
}
