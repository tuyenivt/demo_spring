package com.example.temporal.controller;

import com.example.temporal.config.TemporalProperties;
import com.example.temporal.dto.ErrorResponse;
import com.example.temporal.workflows.ApprovalWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * REST API for human-in-the-loop approval workflows.
 * <p>
 * Demonstrates Workflow.await() — the workflow pauses and waits for an
 * approval or reject signal from an external actor (manager, system, etc.).
 * <p>
 * Example flow:
 * <pre>
 *   POST /api/approvals?orderId=ORD-123&amp;customerId=CUST-1&amp;amount=9999
 *   → returns workflowId: "approval-ORD-123"
 *
 *   POST /api/approvals/ORD-123/approve   {"note":"Looks good"}
 *   → workflow resumes, sends approval notification
 *
 *   GET  /api/approvals/ORD-123/status
 *   → {"approvalStatus":"APPROVED"}
 * </pre>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final TemporalProperties temporalProperties;
    private final WorkflowClient workflowClient;

    /**
     * Start an approval workflow that pauses and waits for a manager decision.
     * <p>
     * curl -X POST "http://localhost:8080/api/approvals?orderId=ORD-123&customerId=CUST-1&amount=9999"
     */
    @PostMapping
    public ResponseEntity<?> startApproval(@RequestParam String orderId, @RequestParam String customerId, @RequestParam long amount) {
        var workflowId = "approval-" + orderId;

        log.info("Starting approval workflow: workflowId={}, orderId={}", workflowId, orderId);

        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalProperties.getTaskQueue())
                .setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofHours(25)) // slightly longer than the internal 24h timeout
                .build();

        var workflow = workflowClient.newWorkflowStub(ApprovalWorkflow.class, options);
        var execution = WorkflowClient.start(() -> workflow.requestApproval(orderId, customerId, amount));

        log.info("Approval workflow started: workflowId={}, runId={}", execution.getWorkflowId(), execution.getRunId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "workflowId", workflowId,
                "orderId", orderId,
                "message", "Approval workflow started. Use /approve or /reject endpoints.",
                "approveUrl", "/api/approvals/" + orderId + "/approve",
                "rejectUrl", "/api/approvals/" + orderId + "/reject",
                "statusUrl", "/api/approvals/" + orderId + "/status"
        ));
    }

    /**
     * Send approve signal to a waiting approval workflow.
     * <p>
     * curl -X POST http://localhost:8080/api/approvals/ORD-123/approve \
     * -H "Content-Type: application/json" \
     * -d '{"note":"Approved by manager"}'
     */
    @PostMapping("/{orderId}/approve")
    public ResponseEntity<?> approve(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        var workflowId = "approval-" + orderId;
        var note = body.getOrDefault("note", "Approved");

        log.info("Sending approve signal: workflowId={}, note={}", workflowId, note);

        try {
            var stub = workflowClient.newWorkflowStub(ApprovalWorkflow.class, workflowId);
            stub.approve(note);
            return ResponseEntity.accepted().body(Map.of("message", "Approve signal sent", "orderId", orderId));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Approval workflow not found: " + orderId));
        }
    }

    /**
     * Send reject signal to a waiting approval workflow.
     * <p>
     * curl -X POST http://localhost:8080/api/approvals/ORD-123/reject \
     * -H "Content-Type: application/json" \
     * -d '{"reason":"Amount exceeds limit"}'
     */
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<?> reject(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        var workflowId = "approval-" + orderId;
        var reason = body.getOrDefault("reason", "Rejected");

        log.info("Sending reject signal: workflowId={}, reason={}", workflowId, reason);

        try {
            var stub = workflowClient.newWorkflowStub(ApprovalWorkflow.class, workflowId);
            stub.reject(reason);
            return ResponseEntity.accepted().body(Map.of("message", "Reject signal sent", "orderId", orderId));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Approval workflow not found: " + orderId));
        }
    }

    /**
     * Query the approval status of a running workflow.
     * <p>
     * curl http://localhost:8080/api/approvals/ORD-123/status
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getStatus(@PathVariable String orderId) {
        var workflowId = "approval-" + orderId;

        try {
            var stub = workflowClient.newWorkflowStub(ApprovalWorkflow.class, workflowId);
            return ResponseEntity.ok(Map.of("orderId", orderId, "approvalStatus", stub.getApprovalStatus()));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Approval workflow not found: " + orderId));
        } catch (Exception e) {
            log.error("Failed to query approval status: workflowId={}, error={}", workflowId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to query: " + e.getMessage()));
        }
    }
}
