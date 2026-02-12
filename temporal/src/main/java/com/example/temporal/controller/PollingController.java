package com.example.temporal.controller;

import com.example.temporal.config.TemporalProperties;
import com.example.temporal.dto.ErrorResponse;
import com.example.temporal.workflows.PollingWorkflow;
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
 * REST API for the continue-as-new polling workflow.
 * <p>
 * Demonstrates {@code Workflow.continueAsNew()} — a long-running polling loop
 * that resets its event history every N iterations to prevent unbounded growth.
 * The workflow ID stays the same across continues; only the run ID changes.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/polling")
public class PollingController {
    private final TemporalProperties temporalProperties;
    private final WorkflowClient workflowClient;

    /**
     * Start a polling workflow for a given target.
     * <p>
     * curl -X POST "http://localhost:8080/api/polling/start?targetId=resource-42"
     */
    @PostMapping("/start")
    public ResponseEntity<?> startPolling(@RequestParam String targetId) {
        var workflowId = "polling-" + targetId;

        log.info("Starting polling workflow: workflowId={}, targetId={}", workflowId, targetId);

        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalProperties.getTaskQueue())
                .setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofHours(1))
                .build();

        try {
            var workflow = workflowClient.newWorkflowStub(PollingWorkflow.class, options);
            var execution = WorkflowClient.start(() -> workflow.startPolling(targetId, 0));

            log.info("Polling workflow started: workflowId={}, runId={}", execution.getWorkflowId(), execution.getRunId());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "workflowId", workflowId,
                    "targetId", targetId,
                    "message", "Polling workflow started. It will continueAsNew every 10 iterations.",
                    "statusUrl", "/api/polling/" + targetId + "/status",
                    "stopUrl", "/api/polling/" + targetId + "/stop"
            ));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Polling workflow already running for target: " + targetId));
            }
            log.error("Failed to start polling workflow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to start polling: " + e.getMessage()));
        }
    }

    /**
     * Query the current iteration count of a polling workflow.
     * <p>
     * curl http://localhost:8080/api/polling/resource-42/status
     */
    @GetMapping("/{targetId}/status")
    public ResponseEntity<?> getStatus(@PathVariable String targetId) {
        var workflowId = "polling-" + targetId;

        try {
            var stub = workflowClient.newWorkflowStub(PollingWorkflow.class, workflowId);
            return ResponseEntity.ok(Map.of("targetId", targetId, "iterationCount", stub.getIterationCount()));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Polling workflow not found for target: " + targetId));
        } catch (Exception e) {
            log.error("Failed to query polling status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to query: " + e.getMessage()));
        }
    }

    /**
     * Stop a polling workflow by terminating it.
     * <p>
     * curl -X DELETE http://localhost:8080/api/polling/resource-42/stop
     */
    @DeleteMapping("/{targetId}/stop")
    public ResponseEntity<?> stopPolling(@PathVariable String targetId) {
        var workflowId = "polling-" + targetId;

        try {
            var stub = workflowClient.newUntypedWorkflowStub(workflowId);
            stub.terminate("Manually stopped via REST API");
            log.info("Polling workflow terminated: workflowId={}", workflowId);
            return ResponseEntity.ok(Map.of("message", "Polling workflow terminated", "targetId", targetId));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Polling workflow not found for target: " + targetId));
        } catch (Exception e) {
            log.error("Failed to stop polling workflow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to stop: " + e.getMessage()));
        }
    }
}
