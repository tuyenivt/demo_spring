package com.example.temporal.dto;

/**
 * Response DTO for order workflow operations.
 */
public record OrderResponse(
        String orderId,
        String workflowId,
        String runId,
        String status,
        String message,
        String url
) {
    public static OrderResponse started(String baseUrl, String orderId, String workflowId, String runId) {
        var url = String.format("%s/namespaces/default/workflows/%s/%s", baseUrl, workflowId, runId);
        return new OrderResponse(orderId, workflowId, runId, "STARTED", "Order workflow started successfully", url);
    }

    public static OrderResponse completed(String orderId, String workflowId, String result) {
        return new OrderResponse(orderId, workflowId, null, "COMPLETED", result, null);
    }

    public static OrderResponse failed(String orderId, String workflowId, String error) {
        return new OrderResponse(orderId, workflowId, null, "FAILED", error, null);
    }
}
