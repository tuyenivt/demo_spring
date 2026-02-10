package com.example.modulith.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class CustomerModuleTest {

    @Autowired
    private CustomerFacade customerFacade;

    @Test
    void bootstrapsCustomerModule() {
        assertThat(customerFacade).isNotNull();
    }
}
