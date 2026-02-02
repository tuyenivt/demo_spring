package com.coloza.sample.rabbitmq.consumer;

import com.coloza.sample.rabbitmq.config.RabbitMQConfig;
import com.coloza.sample.rabbitmq.dto.RpcRequest;
import com.coloza.sample.rabbitmq.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RpcConsumer {

    @RabbitListener(queues = RabbitMQConfig.RPC_QUEUE)
    public RpcResponse handleRpcRequest(RpcRequest request) {
        log.info("Received RPC request: {}", request);
        var response = new RpcResponse(request.getId(), "Reply to: " + request.getMessage());
        log.info("Sending RPC response: {}", response);
        return response;
    }
}
