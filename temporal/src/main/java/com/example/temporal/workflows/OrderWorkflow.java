package com.example.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Main workflow interface for order processing.
 * <p>
 * TEMPORAL CONCEPT: Workflow Interface
 * - Defines the contract for workflow execution
 * - Must be deterministic - same input always produces same output
 * - Cannot perform I/O operations directly (must use Activities)
 * - Can orchestrate Activities and Child Workflows
 */
@WorkflowInterface
public interface OrderWorkflow {

    /**
     * Main workflow method that processes an order.
     *
     * @param orderId    Unique order identifier
     * @param customerId Customer identifier
     * @param amount     Order amount in cents
     * @return Order processing result
     */
    @WorkflowMethod
    String processOrder(String orderId, String customerId, long amount);
}
