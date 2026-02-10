package com.example.modulith.order.infrastructure.web;

import com.example.modulith.order.CreateOrderCommand;
import com.example.modulith.order.OrderFacade;
import com.example.modulith.order.OrderResponse;
import com.example.modulith.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Fetches order details by ID")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
        var response = orderFacade.getOrder(orderId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Returns paginated orders, optionally filtered by customer ID")
    public ApiResponse<Page<OrderResponse>> listOrders(@RequestParam(required = false) Long customerId, Pageable pageable) {
        var response = orderFacade.listOrders(customerId, pageable);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm order", description = "Confirms a pending order")
    public ApiResponse<OrderResponse> confirmOrder(@PathVariable Long orderId) {
        var response = orderFacade.confirmOrder(orderId);
        return ApiResponse.success("Order confirmed", response);
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels a pending or confirmed order")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        var response = orderFacade.cancelOrder(orderId);
        return ApiResponse.success("Order cancelled", response);
    }

    @PatchMapping("/{orderId}/complete")
    @Operation(summary = "Complete order", description = "Completes a confirmed order")
    public ApiResponse<OrderResponse> completeOrder(@PathVariable Long orderId) {
        var response = orderFacade.completeOrder(orderId);
        return ApiResponse.success("Order completed", response);
    }
}
