package com.example.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Child workflow for inventory reservation.
 * <p>
 * Manages the complex process of:
 * 1. Checking inventory availability
 * 2. Reserving items
 * 3. Handling allocation across multiple warehouses (future enhancement)
 */
@WorkflowInterface
public interface InventoryChildWorkflow {

    @WorkflowMethod
    String reserveInventory(String orderId, int quantity);
}
