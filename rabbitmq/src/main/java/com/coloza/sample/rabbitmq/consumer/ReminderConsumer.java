package com.coloza.sample.rabbitmq.consumer;

import com.coloza.sample.rabbitmq.config.RabbitMQConfig;
import com.coloza.sample.rabbitmq.dto.Reminder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class ReminderConsumer {

    @RabbitListener(queues = RabbitMQConfig.REMINDER_QUEUE)
    public void handleReminder(Reminder reminder) {
        log.info("[REMINDER] Delayed reminder received at {}: {}", Instant.now(), reminder);
        log.info("[REMINDER] Sending notification to user {}: {}", reminder.getUserId(), reminder.getMessage());
    }
}
