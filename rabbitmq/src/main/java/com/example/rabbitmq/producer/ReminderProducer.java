package com.example.rabbitmq.producer;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.dto.Reminder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void scheduleReminder(Reminder reminder) {
        log.info("Scheduling delayed reminder (will be delivered after {} ms): {}", RabbitMQConfig.REMINDER_DELAY_MS, reminder);
        rabbitTemplate.convertAndSend(RabbitMQConfig.REMINDER_DELAY_QUEUE, reminder);
    }
}
