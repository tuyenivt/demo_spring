package com.example.rabbitmq.producer;

import com.example.rabbitmq.config.RabbitMQConfig;
import com.example.rabbitmq.dto.RpcRequest;
import com.example.rabbitmq.dto.RpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RpcProducer {

    private final RabbitTemplate rabbitTemplate;

    public RpcResponse sendAndReceive(RpcRequest request) {
        log.info("Sending RPC request: {}", request);
        RpcResponse response = rabbitTemplate.convertSendAndReceiveAsType(
                RabbitMQConfig.RPC_EXCHANGE,
                "rpc.request.message",
                request,
                new ParameterizedTypeReference<>() {
                }
        );
        log.info("Received RPC response: {}", response);
        return response;
    }
}
