package com.example.temporal.workflows.impl;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.workflows.InventoryChildWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Implementation of inventory reservation child workflow.
 */
@Slf4j
public class InventoryChildWorkflowImpl implements InventoryChildWorkflow {

    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(20))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(3)
                    .build())
            .build();

    private final OrderActivities activities = Workflow.newActivityStub(OrderActivities.class, activityOptions);

    @Override
    public String reserveInventory(String orderId, int quantity) {
        log.info("Inventory child workflow started for orderId={}, quantity={}", orderId, quantity);

        try {
            // Check availability
            log.info("Checking inventory availability");
            var available = activities.checkInventory(orderId, quantity);

            if (!available) {
                var msg = "Insufficient inventory for orderId=" + orderId;
                log.error(msg);
                throw new IllegalStateException(msg);
            }

            // Reserve inventory
            log.info("Reserving inventory");
            activities.reserveInventory(orderId, quantity);
            log.info("Inventory reserved successfully");

            return "Inventory reserved: " + quantity + " items";
        } catch (Exception e) {
            log.error("Inventory reservation failed for orderId={}: {}", orderId, e.getMessage());
            throw e;
        }
    }
}
