package com.example.modulith.customer;

import com.example.modulith.customer.application.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public CustomerResponse getCustomer(Long customerId) {
        return customerService.getCustomer(customerId);
    }

    public Page<CustomerResponse> listCustomers(Pageable pageable) {
        return customerService.listCustomers(pageable);
    }
}
