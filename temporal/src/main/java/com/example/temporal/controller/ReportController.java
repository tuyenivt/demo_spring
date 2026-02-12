package com.example.temporal.controller;

import com.example.temporal.config.TemporalProperties;
import com.example.temporal.dto.ErrorResponse;
import com.example.temporal.workflows.ReportWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * REST API for managing the daily report cron workflow.
 * <p>
 * Demonstrates Temporal's cron scheduling as an alternative to Spring {@code @Scheduled}
 * and external cron systems (crontab, Kubernetes CronJobs).
 * <p>
 * Advantages over {@code @Scheduled}:
 * - Runs only on one instance (no duplicate runs in a cluster)
 * - Durable: missed runs are tracked, not silently dropped
 * - Observable: view history and results in the Temporal UI
 * - Retryable: automatically retries failed runs on next schedule
 * <p>
 * Usage:
 * <pre>
 *   POST /api/reports/start    → start cron workflow (runs daily at 9 AM UTC)
 *   DELETE /api/reports/stop   → terminate the cron workflow
 * </pre>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private static final String REPORT_WORKFLOW_ID = "daily-order-report";

    private final TemporalProperties temporalProperties;
    private final WorkflowClient workflowClient;

    /**
     * Start the daily report cron workflow.
     * Runs every day at 9 AM UTC. Only one instance runs at a time.
     * <p>
     * curl -X POST http://localhost:8080/api/reports/start
     */
    @PostMapping("/start")
    public ResponseEntity<?> startCronReport() {
        log.info("Starting daily report cron workflow: workflowId={}", REPORT_WORKFLOW_ID);

        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalProperties.getTaskQueue())
                .setWorkflowId(REPORT_WORKFLOW_ID)
                // CRON SCHEDULE: standard cron expression — 9 AM UTC daily
                // Temporal evaluates this in UTC by default
                .setCronSchedule("0 9 * * *")
                // Per-run execution timeout (does NOT apply across cron runs)
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build();

        try {
            var workflow = workflowClient.newWorkflowStub(ReportWorkflow.class, options);
            var execution = WorkflowClient.start(workflow::generateDailyReport);

            log.info("Cron workflow started: workflowId={}, runId={}", execution.getWorkflowId(), execution.getRunId());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "workflowId", REPORT_WORKFLOW_ID,
                    "schedule", "0 9 * * * (daily at 9 AM UTC)",
                    "message", "Cron report workflow started. Use /stop to terminate."
            ));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                        "Cron workflow already running. Use /stop first to restart."));
            }
            log.error("Failed to start cron workflow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to start cron: " + e.getMessage()));
        }
    }

    /**
     * Stop the daily report cron workflow by terminating it.
     * <p>
     * curl -X DELETE http://localhost:8080/api/reports/stop
     */
    @DeleteMapping("/stop")
    public ResponseEntity<?> stopCronReport() {
        log.info("Stopping daily report cron workflow: workflowId={}", REPORT_WORKFLOW_ID);

        try {
            var stub = workflowClient.newUntypedWorkflowStub(REPORT_WORKFLOW_ID);
            stub.terminate("Manually stopped via REST API");
            log.info("Cron workflow terminated: workflowId={}", REPORT_WORKFLOW_ID);
            return ResponseEntity.ok(Map.of("message", "Cron workflow terminated", "workflowId", REPORT_WORKFLOW_ID));
        } catch (WorkflowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Cron workflow not running"));
        } catch (Exception e) {
            log.error("Failed to stop cron workflow: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to stop cron: " + e.getMessage()));
        }
    }
}
