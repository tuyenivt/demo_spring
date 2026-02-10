package com.example.modulith.customer.application;

import com.example.modulith.customer.*;
import com.example.modulith.customer.domain.Customer;
import com.example.modulith.customer.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new DuplicateEmailException(command.email());
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

    @Transactional(readOnly = true)
    public boolean customerExists(Long customerId) {
        return customerRepository.existsById(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToResponse);
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
