package com.example.modulith.order.application;

import com.example.modulith.customer.CustomerFacade;
import com.example.modulith.order.CreateOrderCommand;
import com.example.modulith.order.OrderCreatedEvent;
import com.example.modulith.order.OrderResponse;
import com.example.modulith.order.domain.Order;
import com.example.modulith.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerFacade customerFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        // Verify customer exists by calling customer module's public API
        if (!customerFacade.customerExists(command.customerId())) {
            throw new IllegalArgumentException("Customer with ID " + command.customerId() + " does not exist");
        }

        var order = new Order(command.customerId(), command.totalAmount(), command.sku(), command.quantity());
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getSku(),
                order.getQuantity(),
                order.getCreatedAt()
        ));

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .sku(order.getSku())
                .quantity(order.getQuantity())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
