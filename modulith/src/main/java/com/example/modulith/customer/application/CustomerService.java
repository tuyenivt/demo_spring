package com.example.modulith.customer.application;

import com.example.modulith.customer.CustomerRegisteredEvent;
import com.example.modulith.customer.CustomerResponse;
import com.example.modulith.customer.RegisterCustomerCommand;
import com.example.modulith.customer.domain.Customer;
import com.example.modulith.customer.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerCommand command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Customer with email " + command.email() + " already exists");
        }

        var customer = new Customer(command.name(), command.email());
        customer = customerRepository.save(customer);

        // Publish domain event for other modules to react to
        eventPublisher.publishEvent(new CustomerRegisteredEvent(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getRegisteredAt()
        ));

        return mapToResponse(customer);
    }

    public boolean customerExists(Long customerId) {
        return customerRepository.existsById(customerId);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .registeredAt(customer.getRegisteredAt())
                .build();
    }
}
