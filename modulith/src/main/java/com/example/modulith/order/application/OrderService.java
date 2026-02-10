package com.example.modulith.order.application;

import com.example.modulith.customer.CustomerFacade;
import com.example.modulith.order.*;
import com.example.modulith.order.domain.Order;
import com.example.modulith.order.domain.OrderRepository;
import com.example.modulith.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Long customerId, Pageable pageable) {
        var orders = customerId == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByCustomerId(customerId, pageable);

        return orders.map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        var order = getOrderEntity(orderId);
        try {
            order.confirm();
        } catch (IllegalStateException ex) {
            throw new OrderStateTransitionException(order.getId(), order.getStatus().name(), "confirm");
        }

        order = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderConfirmedEvent(order.getId(), order.getSku(), order.getQuantity(), Instant.now()));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        var order = getOrderEntity(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return mapToResponse(order);
        }

        try {
            order.cancel();
        } catch (IllegalStateException ex) {
            throw new OrderStateTransitionException(order.getId(), order.getStatus().name(), "cancel");
        }

        order = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCancelledEvent(order.getId(), order.getSku(), order.getQuantity(), Instant.now()));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        var order = getOrderEntity(orderId);
        try {
            order.complete();
        } catch (IllegalStateException ex) {
            throw new OrderStateTransitionException(order.getId(), order.getStatus().name(), "complete");
        }

        order = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCompletedEvent(order.getId(), order.getSku(), order.getQuantity(), Instant.now()));
        return mapToResponse(order);
    }

    private Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
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
