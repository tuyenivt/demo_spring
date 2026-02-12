package com.example.temporal.workflows.impl;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.workflows.ApprovalWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Human-in-the-loop approval workflow implementation.
 * <p>
 * KEY PATTERN: {@code Workflow.await(timeout, condition)}
 * <p>
 * The workflow pauses at the await call and resumes when:
 * 1. A signal sets {@code approved} or {@code rejected} to true, OR
 * 2. The timeout expires (auto-reject path)
 * <p>
 * Why this is powerful:
 * - The pause is durable: worker restarts, deploys, and crashes do NOT lose the wait
 * - The condition is re-evaluated after every signal arrival
 * - Always set a timeout to avoid permanently blocked workflows
 * <p>
 * Best practices:
 * - Use a boolean flag + {@code Workflow.await(() -> flag)} for signal-based unblocking
 * - Return value of {@code Workflow.await()} indicates whether condition was met (true) or timed out (false)
 * - Combine with queries so the UI can show "pending approval" state
 */
@Slf4j
public class ApprovalWorkflowImpl implements ApprovalWorkflow {

    private static final Duration APPROVAL_TIMEOUT = Duration.ofHours(24);

    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setMaximumAttempts(2)
                    .build())
            .build();

    private final OrderActivities activities = Workflow.newActivityStub(OrderActivities.class, activityOptions);

    // State modified by signals
    private boolean approved = false;
    private boolean rejected = false;
    private String approverNote;
    private String rejectionReason;
    private String approvalStatus = "PENDING_APPROVAL";

    @Override
    public String requestApproval(String orderId, String customerId, long amount) {
        log.info("Approval workflow started: orderId={}, customerId={}, amount={}", orderId, customerId, amount);

        // Notify the approver that a decision is needed
        activities.sendNotification(customerId, "Order " + orderId + " for amount " + amount + " requires your approval.");

        // WORKFLOW AWAIT: pause until approved, rejected, or timeout.
        // This is durable — the workflow survives restarts while waiting.
        // The condition lambda is evaluated after each signal arrives.
        log.info("Waiting for approval decision (timeout: {}h)", APPROVAL_TIMEOUT.toHours());
        boolean decisionReceived = Workflow.await(APPROVAL_TIMEOUT, () -> approved || rejected);

        if (!decisionReceived) {
            // Timeout expired without a decision → auto-reject
            approvalStatus = "AUTO_REJECTED";
            log.info("Approval timed out for orderId={} — auto-rejecting", orderId);
            activities.sendNotification(customerId, "Order " + orderId + " was automatically rejected due to approval timeout.");
            return "Order " + orderId + " rejected: approval timeout after " + APPROVAL_TIMEOUT.toHours() + " hours";
        }

        if (approved) {
            approvalStatus = "APPROVED";
            log.info("Order approved: orderId={}, note={}", orderId, approverNote);
            activities.sendNotification(customerId, "Order " + orderId + " approved! Note: " + approverNote);
            return "Order " + orderId + " approved: " + approverNote;
        } else {
            approvalStatus = "REJECTED";
            log.info("Order rejected: orderId={}, reason={}", orderId, rejectionReason);
            activities.sendNotification(customerId, "Order " + orderId + " rejected. Reason: " + rejectionReason);
            return "Order " + orderId + " rejected: " + rejectionReason;
        }
    }

    @Override
    public void approve(String note) {
        log.info("Received approve signal: note={}", note);
        this.approverNote = note;
        this.approved = true;
        this.approvalStatus = "APPROVED";
    }

    @Override
    public void reject(String reason) {
        log.info("Received reject signal: reason={}", reason);
        this.rejectionReason = reason;
        this.rejected = true;
        this.approvalStatus = "REJECTED";
    }

    @Override
    public String getApprovalStatus() {
        return approvalStatus;
    }
}
