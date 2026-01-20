package com.example.modulith.customer;

import com.example.modulith.customer.application.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Public API facade for the customer module.
 * Other modules should interact with customer module through this interface.
 */
@Component
@RequiredArgsConstructor
public class CustomerFacade {

    private final CustomerService customerService;

    public CustomerResponse registerCustomer(RegisterCustomerCommand command) {
        return customerService.registerCustomer(command);
    }

    public boolean customerExists(Long customerId) {
        return customerService.customerExists(customerId);
    }
}
