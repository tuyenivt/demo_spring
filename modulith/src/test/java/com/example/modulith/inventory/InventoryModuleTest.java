package com.example.modulith.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class InventoryModuleTest {

    @Autowired
    private InventoryFacade inventoryFacade;

    @Test
    void bootstrapsInventoryModule() {
        assertThat(inventoryFacade).isNotNull();
    }
}
