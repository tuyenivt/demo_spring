package com.example.temporal.workflows;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Long-running polling workflow that demonstrates {@code Workflow.continueAsNew()}.
 * <p>
 * The problem with long-running workflows:
 * Temporal stores every event (activity result, signal, timer) in an event history log.
 * A workflow that runs 1,000 iterations accumulates thousands of events. Large histories
 * slow down replay (Temporal re-executes history on worker restart) and consume storage.
 * <p>
 * The solution: {@code Workflow.continueAsNew(args...)}
 * - Starts a fresh workflow run with new event history
 * - Passes current state as arguments to the new run
 * - The workflow ID stays the same; only the run ID changes
 * - Callers see it as one logical workflow
 * <p>
 * Best practices:
 * - Reset history every N iterations (e.g. 100) or when history exceeds ~10,000 events
 * - Pass only essential state (avoid large payloads — they slow down replay)
 * - Test that state transfers correctly across continue-as-new boundaries
 */
@WorkflowInterface
public interface PollingWorkflow {

    /**
     * Start the polling loop.
     *
     * @param targetId       the resource to poll
     * @param iterationCount current iteration count (0 for first run; carried across continueAsNew)
     * @return final result when polling completes
     */
    @WorkflowMethod
    String startPolling(String targetId, int iterationCount);

    /**
     * Query total iterations completed so far (across all continues).
     */
    @QueryMethod
    int getIterationCount();
}
