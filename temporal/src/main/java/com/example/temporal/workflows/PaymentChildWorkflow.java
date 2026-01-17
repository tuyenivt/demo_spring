package com.example.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Child workflow for payment processing.
 * <p>
 * WHY USE A CHILD WORKFLOW?
 * <p>
 * 1. SEPARATION OF CONCERNS
 * - Payment logic is complex enough to deserve its own workflow
 * - Can be tested independently
 * - Can be versioned separately from parent workflow
 * <p>
 * 2. REUSABILITY
 * - Can be called from multiple parent workflows
 * - Encapsulates payment domain logic
 * <p>
 * 3. INDEPENDENT LIFECYCLE
 * - Has its own execution history
 * - Can have different timeout and retry policies
 * - Failure doesn't immediately fail parent (parent can handle it)
 * <p>
 * CHILD WORKFLOW vs ACTIVITY:
 * - Use Activities for: Simple operations, I/O calls, stateless operations
 * - Use Child Workflows for: Complex multistep processes, need for versioning,
 * reusable business processes, operations that benefit from workflow features
 */
@WorkflowInterface
public interface PaymentChildWorkflow {

    @WorkflowMethod
    String processPayment(String orderId, String customerId, long amount);
}
