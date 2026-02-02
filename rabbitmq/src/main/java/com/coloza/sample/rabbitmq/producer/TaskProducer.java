package com.coloza.sample.rabbitmq.producer;

import com.coloza.sample.rabbitmq.config.RabbitMQConfig;
import com.coloza.sample.rabbitmq.dto.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProducer {

    private final RabbitTemplate rabbitTemplate;

    public void submitTask(Task task) {
        log.info("Submitting task to work queue: {}", task);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_QUEUE, task);
    }
}
