package com.example.temporal.workflows;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Human-in-the-loop approval workflow.
 * <p>
 * Demonstrates {@code Workflow.await()} — the workflow pauses execution and waits
 * for an external signal before proceeding. This is Temporal's pattern for:
 * - Manager approval flows
 * - Two-step verification
 * - Timed offers (auto-expire without a decision)
 * <p>
 * Flow:
 * <pre>
 *   POST /api/approvals          → Start ApprovalWorkflow (returns workflowId)
 *   POST /api/approvals/{id}/approve  → Send approve signal → workflow completes
 *   POST /api/approvals/{id}/reject   → Send reject signal  → workflow fails fast
 *   GET  /api/approvals/{id}/status   → Query current state
 * </pre>
 * If neither signal arrives within 24 hours, the workflow auto-rejects.
 */
@WorkflowInterface
public interface ApprovalWorkflow {

    @WorkflowMethod
    String requestApproval(String orderId, String customerId, long amount);

    /**
     * Signal sent by an approver to approve the order.
     */
    @SignalMethod
    void approve(String approverNote);

    /**
     * Signal sent by an approver to reject the order.
     */
    @SignalMethod
    void reject(String rejectionReason);

    /**
     * Query current approval state.
     */
    @QueryMethod
    String getApprovalStatus();
}
