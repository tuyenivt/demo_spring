package com.example.rabbitmq.producer;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.dto.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void broadcast(Notification notification) {
        log.info("Broadcasting notification to all subscribers: {}", notification);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_FANOUT_EXCHANGE,
                "", // Routing key is ignored for fanout exchange
                notification
        );
    }
}
