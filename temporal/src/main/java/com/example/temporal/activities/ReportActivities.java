package com.example.temporal.activities;

import io.temporal.activity.ActivityInterface;

/**
 * Activities for generating order summary reports.
 * Used by the {@link com.example.temporal.workflows.ReportWorkflow} cron workflow.
 */
@ActivityInterface
public interface ReportActivities {

    /**
     * Aggregate order data and generate a summary report.
     *
     * @param reportDate ISO date string (e.g. "2024-01-15")
     * @return summary string (order count, total revenue, etc.)
     */
    String generateOrderReport(String reportDate);
}
