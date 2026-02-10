package com.example.modulith.order;

import com.example.modulith.order.application.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;

    public OrderResponse createOrder(CreateOrderCommand command) {
        return orderService.createOrder(command);
    }

    public OrderResponse getOrder(Long orderId) {
        return orderService.getOrder(orderId);
    }

    public Page<OrderResponse> listOrders(Long customerId, Pageable pageable) {
        return orderService.listOrders(customerId, pageable);
    }

    public OrderResponse confirmOrder(Long orderId) {
        return orderService.confirmOrder(orderId);
    }

    public OrderResponse cancelOrder(Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    public OrderResponse completeOrder(Long orderId) {
        return orderService.completeOrder(orderId);
    }
}
