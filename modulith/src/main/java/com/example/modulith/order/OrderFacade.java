package com.example.modulith.order;

import com.example.modulith.order.application.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;

    public OrderResponse createOrder(CreateOrderCommand command) {
        return orderService.createOrder(command);
    }
}
