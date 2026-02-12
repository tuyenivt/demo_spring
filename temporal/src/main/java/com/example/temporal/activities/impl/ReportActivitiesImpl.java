package com.example.temporal.activities.impl;

import com.example.temporal.activities.ReportActivities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Implementation of report activities.
 * In a real system, this would query a database and send reports via email/Slack.
 */
@Slf4j
@Component
public class ReportActivitiesImpl implements ReportActivities {

    private final Random random = new Random();

    @Override
    public String generateOrderReport(String reportDate) {
        log.info("Generating order report for date: {}", reportDate);

        // Simulate report generation (in production: query DB, aggregate, send)
        var orderCount = random.nextInt(50) + 10;
        var totalRevenue = (long) orderCount * (random.nextInt(5000) + 1000);

        var report = String.format("Report[%s]: orders=%d, revenue=%d", reportDate, orderCount, totalRevenue);
        log.info("Report generated: {}", report);
        return report;
    }
}
