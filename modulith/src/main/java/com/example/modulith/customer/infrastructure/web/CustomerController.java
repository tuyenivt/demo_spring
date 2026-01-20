package com.example.modulith.customer.infrastructure.web;

import com.example.modulith.customer.CustomerFacade;
import com.example.modulith.customer.CustomerResponse;
import com.example.modulith.customer.RegisterCustomerCommand;
import com.example.modulith.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management API")
class CustomerController {

    private final CustomerFacade customerFacade;

    @PostMapping
    @Operation(summary = "Register a new customer", description = "Creates a new customer account")
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(@Valid @RequestBody RegisterCustomerCommand command) {
        var response = customerFacade.registerCustomer(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer registered successfully", response));
    }

    @GetMapping("/{customerId}/exists")
    @Operation(summary = "Check if customer exists", description = "Verifies if a customer exists by ID")
    public ApiResponse<Boolean> customerExists(@PathVariable Long customerId) {
        var exists = customerFacade.customerExists(customerId);
        return ApiResponse.success(exists);
    }
}
