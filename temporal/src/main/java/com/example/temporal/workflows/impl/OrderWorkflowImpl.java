package com.example.temporal.workflows.impl;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.workflows.InventoryChildWorkflow;
import com.example.temporal.workflows.OrderWorkflow;
import com.example.temporal.workflows.PaymentChildWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Implementation of the main order processing workflow.
 * <p>
 * KEY TEMPORAL CONCEPTS DEMONSTRATED:
 * <p>
 * 1. DETERMINISTIC EXECUTION
 * - Workflow code must be deterministic (same input → same output)
 * - No direct I/O operations (database calls, HTTP requests, random numbers, current time)
 * - Use Workflow.getLogger() for logging (replayed logs are suppressed)
 * - Use Workflow.sleep() instead of Thread.sleep()
 * - Use Workflow.currentTimeMillis() instead of System.currentTimeMillis()
 * <p>
 * 2. ACTIVITY RETRY vs WORKFLOW RETRY
 * - Activity Retry: Retries individual activity executions (configured in ActivityOptions)
 * - Workflow Retry: Retries the entire workflow if it fails (configured when starting workflow)
 * - Activities handle transient failures (network issues, temporary service outages)
 * - Workflow retry handles workflow-level failures (unhandled exceptions, timeouts)
 * <p>
 * 3. CHILD WORKFLOWS
 * - Used for complex sub-processes that need their own lifecycle
 * - Can be versioned independently
 * - Have their own retry policies
 * - Useful for reusable business processes
 */
@Slf4j
public class OrderWorkflowImpl implements OrderWorkflow {
    /**
     * Activity options with retry policy.
     * <p>
     * ACTIVITY RETRY POLICY:
     * - Initial interval: 1 second
     * - Backoff coefficient: 2.0 (exponential backoff)
     * - Maximum interval: 10 seconds
     * - Maximum attempts: 3
     * <p>
     * This means:
     * - Attempt 1: Immediate
     * - Attempt 2: After 1 second
     * - Attempt 3: After 2 seconds
     * - If all fail, activity throws ActivityFailure
     */
    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30)) // Max time for activity to complete
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setBackoffCoefficient(2.0)
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setMaximumAttempts(3) // Total attempts (initial + 2 retries)
                    .build())
            .build();

    /**
     * Child workflow options with retry policy.
     * <p>
     * CHILD WORKFLOW RETRY POLICY:
     * - Similar to activity retry, but for entire child workflow execution
     * - If child workflow fails after retries, parent workflow can handle the failure
     */
    private final ChildWorkflowOptions childWorkflowOptions = ChildWorkflowOptions.newBuilder()
            .setWorkflowExecutionTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(2)
                    .build())
            .build();

    // Activity stub - proxy for executing activities
    private final OrderActivities activities = Workflow.newActivityStub(OrderActivities.class, activityOptions);

    @Override
    public String processOrder(String orderId, String customerId, long amount) {
        log.info("Starting order processing for orderId={}, customerId={}, amount={}",
                orderId, customerId, amount);

        try {
            // Step 1: Validate order (Activity)
            // This activity has retry policy from activityOptions
            log.info("Step 1: Validating order");
            activities.validateOrder(orderId, amount);
            log.info("Order validation completed successfully");

            // Step 2: Process payment via Child Workflow
            // Child workflows are useful for complex sub-processes
            // They have their own execution history and can be versioned independently
            log.info("Step 2: Processing payment");
            var paymentWorkflow = Workflow.newChildWorkflowStub(PaymentChildWorkflow.class, childWorkflowOptions);

            var paymentResult = paymentWorkflow.processPayment(orderId, customerId, amount);
            log.info("Payment processing completed: {}", paymentResult);

            // Step 3: Reserve inventory via Child Workflow
            log.info("Step 3: Reserving inventory");
            var inventoryWorkflow = Workflow.newChildWorkflowStub(InventoryChildWorkflow.class, childWorkflowOptions);

            var inventoryResult = inventoryWorkflow.reserveInventory(orderId, 1);
            log.info("Inventory reservation completed: {}", inventoryResult);

            // Step 4: Send confirmation notification (Activity)
            log.info("Step 4: Sending confirmation");
            activities.sendNotification(customerId, "Order " + orderId + " confirmed!");
            log.info("Notification sent successfully");

            var result = "Order " + orderId + " processed successfully";
            log.info("Order processing completed: {}", result);
            return result;

        } catch (ActivityFailure e) {
            // Activity failed after all retries
            // This demonstrates ACTIVITY RETRY failure
            log.error("Activity failed after retries for orderId={}: {}", orderId, e.getMessage());

            // In production, you might want to:
            // 1. Execute compensation activities (refund, release inventory)
            // 2. Send alerts
            // 3. Update order status to "failed"
            throw e; // Re-throw to fail the workflow
        } catch (Exception e) {
            // Other workflow failures
            // If workflow retry policy is configured, the entire workflow will retry
            log.error("Workflow failed for orderId={}: {}", orderId, e.getMessage());
            throw e;
        }
    }
}
