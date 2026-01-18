package com.example.temporal.controller;

import com.example.temporal.config.TemporalProperties;
import com.example.temporal.dto.ErrorResponse;
import com.example.temporal.dto.OrderRequest;
import com.example.temporal.dto.OrderResponse;
import com.example.temporal.workflows.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

/**
 * REST API for order processing workflows.
 * <p>
 * Provides endpoints to:
 * 1. Start new order workflows (async)
 * 2. Start and wait for completion (sync, for demo/testing)
 * 3. Query workflow status
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final TemporalProperties temporalProperties;
    private final WorkflowClient workflowClient;

    /**
     * Start a new order workflow asynchronously.
     * Returns immediately with workflow ID - workflow continues in background.
     * <p>
     * This is the RECOMMENDED approach for production:
     * - Non-blocking
     * - Client doesn't wait for workflow completion
     * - Can handle long-running workflows
     * - Client can poll for status or use webhooks
     * <p>
     * curl -X POST http://localhost:8080/api/orders \
     * -H "Content-Type: application/json" \
     * -d '{"customerId":"CUST-123","amount":9999,"description":"Premium Widget"}'
     */
    @PostMapping
    public ResponseEntity<OrderResponse> startOrder(@RequestBody OrderRequest request) {
        var orderId = generateOrderId();
        var workflowId = "order-workflow-" + orderId;

        log.info("Starting order workflow: orderId={}, customerId={}, amount={}", orderId, request.customerId(), request.amount());

        // Configure workflow options
        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalProperties.getTaskQueue())
                .setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .setRetryOptions(RetryOptions.newBuilder()
                        .setInitialInterval(Duration.ofSeconds(10))
                        .setBackoffCoefficient(2.0)
                        .setMaximumAttempts(3)
                        .build())
                .build();

        // Create workflow stub
        var workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, options);

        // Start workflow asynchronously
        var execution = WorkflowClient.start(() -> workflow.processOrder(orderId, request.customerId(), request.amount()));

        log.info("Order workflow started: orderId={}, workflowId={}, runId={}", orderId, execution.getWorkflowId(), execution.getRunId());

        var response = OrderResponse.started(temporalProperties.getUi(), orderId, execution.getWorkflowId(), execution.getRunId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Start a new order workflow and WAIT for completion (synchronous).
     * Blocks until workflow completes or fails.
     * <p>
     * USE FOR DEMO/TESTING ONLY:
     * - Blocks the HTTP thread
     * - Not suitable for long-running workflows
     * - Client must wait for entire workflow
     * <p>
     * GOOD FOR:
     * - Quick demos
     * - Testing
     * - Short workflows (<30 seconds)
     * <p>
     * curl -X POST http://localhost:8080/api/orders/sync \
     * -H "Content-Type: application/json" \
     * -d '{"customerId":"CUST-456","amount":5000,"description":"Basic Widget"}'
     */
    @PostMapping("/sync")
    public ResponseEntity<OrderResponse> startOrderSync(@RequestBody OrderRequest request) {
        var orderId = generateOrderId();
        var workflowId = "order-workflow-" + orderId;

        log.info("Starting SYNCHRONOUS order workflow: orderId={}, customerId={}, amount={}", orderId, request.customerId(), request.amount());

        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalProperties.getTaskQueue())
                .setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .setRetryOptions(RetryOptions.newBuilder()
                        .setInitialInterval(Duration.ofSeconds(10))
                        .setBackoffCoefficient(2.0)
                        .setMaximumAttempts(3)
                        .build())
                .build();

        var workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, options);

        try {
            // Execute workflow synchronously - BLOCKS until complete
            var result = workflow.processOrder(orderId, request.customerId(), request.amount());

            log.info("Order workflow completed successfully: orderId={}, result={}", orderId, result);

            return ResponseEntity.ok(OrderResponse.completed(orderId, workflowId, result));
        } catch (Exception e) {
            log.error("Order workflow failed: orderId={}, error={}", orderId, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OrderResponse.failed(orderId, workflowId, e.getMessage()));
        }
    }

    /**
     * Get the status of an order workflow.
     * <p>
     * curl http://localhost:8080/api/orders/{ORD-XXXXXXXX}/status
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable String orderId) {
        var workflowId = "order-workflow-" + orderId;

        try {
            // Get untyped workflow stub for the workflow ID
            var workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);

            // Use the stub to describe the workflow execution
            // This is cleaner than calling service stubs directly
            var description = workflowStub.describe();

            var status = description.getWorkflowExecutionInfo().getStatus();
            var runId = description.getWorkflowExecutionInfo().getExecution().getRunId();

            return ResponseEntity.ok(new OrderResponse(
                    orderId,
                    workflowId,
                    runId,
                    status.name(),
                    null,
                    String.format("%s/namespaces/default/workflows/%s/%s", temporalProperties.getUi(),
                            workflowId, runId)
            ));
        } catch (Exception e) {
            log.error("Failed to get workflow status: orderId={}, error={}", orderId, e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Workflow not found: " + orderId));
        }
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
