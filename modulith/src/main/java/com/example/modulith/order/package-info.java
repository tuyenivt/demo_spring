@org.springframework.modulith.ApplicationModule(
        displayName = "Order Module",
        allowedDependencies = {"customer", "shared::api"}
)
package com.example.modulith.order;
