package com.example.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Cron workflow that generates daily order summary reports.
 * <p>
 * Temporal can replace Spring {@code @Scheduled} and external cron systems with
 * durable, distributed scheduling. A cron workflow:
 * - Runs on the configured cron schedule (e.g. "0 9 * * *" for 9 AM daily)
 * - Automatically retries on failure (next scheduled run, not immediately)
 * - Only one execution runs at a time (natural deduplication by workflow ID)
 * - Preserves the previous run result via {@code Workflow.getLastCompletionResult()}
 * <p>
 * The workflow is started once via REST and continues indefinitely until terminated.
 */
@WorkflowInterface
public interface ReportWorkflow {

    /**
     * Generate an order summary report.
     * Called once per cron schedule interval.
     *
     * @return report summary from this run
     */
    @WorkflowMethod
    String generateDailyReport();
}
