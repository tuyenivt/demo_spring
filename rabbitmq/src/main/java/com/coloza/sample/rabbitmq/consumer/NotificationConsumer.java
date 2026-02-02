package com.coloza.sample.rabbitmq.consumer;

import com.coloza.sample.rabbitmq.config.RabbitMQConfig;
import com.coloza.sample.rabbitmq.dto.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_EMAIL_QUEUE)
    public void handleEmailNotification(Notification notification) {
        log.info("[EMAIL] Sending email notification to {}: {}", notification.getRecipient(), notification.getMessage());
        // Simulate email sending
        simulateProcessing("email");
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_SMS_QUEUE)
    public void handleSmsNotification(Notification notification) {
        log.info("[SMS] Sending SMS notification to {}: {}", notification.getRecipient(), notification.getMessage());
        // Simulate SMS sending
        simulateProcessing("sms");
    }

    private void simulateProcessing(String type) {
        try {
            Thread.sleep(100);
            log.info("[{}] Notification sent successfully", type.toUpperCase());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
