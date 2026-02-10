@org.springframework.modulith.ApplicationModule(
        displayName = "Inventory Module",
        allowedDependencies = {"order", "shared::api"}
)
package com.example.modulith.inventory;
