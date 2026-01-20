package com.example.modulith.order.infrastructure.web;

import com.example.modulith.order.CreateOrderCommand;
import com.example.modulith.order.OrderFacade;
import com.example.modulith.order.OrderResponse;
import com.example.modulith.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management API")
class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order for an existing customer")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        var response = orderFacade.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order created successfully", response));
    }
}
