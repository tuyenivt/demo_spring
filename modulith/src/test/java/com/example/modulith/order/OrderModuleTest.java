package com.example.modulith.order;

import com.example.modulith.customer.CustomerFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class OrderModuleTest {

    @MockitoBean
    private CustomerFacade customerFacade;

    @Autowired
    private OrderFacade orderFacade;

    @Test
    void bootstrapsOrderModule() {
        assertThat(orderFacade).isNotNull();
    }
}
