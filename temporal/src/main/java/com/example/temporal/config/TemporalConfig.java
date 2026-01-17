package com.example.temporal.config;

import com.example.temporal.activities.impl.OrderActivitiesImpl;
import com.example.temporal.activities.impl.PaymentActivitiesImpl;
import com.example.temporal.workflows.impl.InventoryChildWorkflowImpl;
import com.example.temporal.workflows.impl.OrderWorkflowImpl;
import com.example.temporal.workflows.impl.PaymentChildWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporal configuration for Spring Boot.
 * <p>
 * Sets up:
 * - WorkflowServiceStubs: Connection to Temporal server
 * - WorkflowClient: Client for starting workflows
 * - Workers: Execute workflows and activities
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TemporalConfig {
    private final TemporalProperties temporalProperties;

    private WorkerFactory workerFactory;

    /**
     * Creates connection to Temporal server.
     * In production, configure with proper TLS and authentication.
     */
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(WorkflowServiceStubsOptions.newBuilder().setTarget(temporalProperties.getTarget()).build());
    }

    /**
     * Creates Temporal client for starting and querying workflows.
     */
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs, WorkflowClientOptions.newBuilder().setNamespace(temporalProperties.getNamespace()).build());
    }

    /**
     * Creates and starts workers that poll for tasks.
     * <p>
     * Workers are responsible for:
     * 1. Polling task queues for workflow tasks and activity tasks
     * 2. Executing workflow code (deterministic orchestration)
     * 3. Executing activity code (non-deterministic I/O operations)
     */
    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient, OrderActivitiesImpl orderActivities, PaymentActivitiesImpl paymentActivities) {
        workerFactory = WorkerFactory.newInstance(workflowClient);

        // Create worker for order processing task queue
        var worker = workerFactory.newWorker(temporalProperties.getTaskQueue());

        // Register workflow implementations
        // These are the orchestration logic - must be deterministic
        worker.registerWorkflowImplementationTypes(OrderWorkflowImpl.class, PaymentChildWorkflowImpl.class, InventoryChildWorkflowImpl.class);

        // Register activity implementations
        // These perform actual I/O operations - can be non-deterministic
        worker.registerActivitiesImplementations(orderActivities, paymentActivities);

        // Start all workers
        workerFactory.start();
        log.info("Temporal workers started on task queue: {}", temporalProperties.getTaskQueue());

        return workerFactory;
    }

    /**
     * Gracefully shutdown workers when application stops.
     */
    @PreDestroy
    public void destroy() {
        if (workerFactory != null) {
            log.info("Shutting down Temporal workers...");
            workerFactory.shutdown();
        }
    }
}
