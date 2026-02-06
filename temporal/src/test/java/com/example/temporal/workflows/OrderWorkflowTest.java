package com.example.temporal.workflows;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.activities.PaymentActivities;
import com.example.temporal.workflows.impl.InventoryChildWorkflowImpl;
import com.example.temporal.workflows.impl.OrderWorkflowImpl;
import com.example.temporal.workflows.impl.PaymentChildWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OrderWorkflow using Temporal's TestWorkflowEnvironment.
 * <p>
 * TESTING CONCEPTS:
 * - TestWorkflowEnvironment provides in-memory Temporal server
 * - Activity implementations are concrete classes (not mocks)
 * - Time skipping available for testing timers and delays
 */
class OrderWorkflowTest {

    private static final String TASK_QUEUE = "test-order-queue";

    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                OrderWorkflowImpl.class,
                PaymentChildWorkflowImpl.class,
                InventoryChildWorkflowImpl.class
        );
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void processOrder_success() {
        // Use stubbed activities that always succeed
        worker.registerActivitiesImplementations(
                new SuccessOrderActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build());

        String result = workflow.processOrder("ORD-123", "CUST-456", 1000L);

        assertThat(result).contains("ORD-123").contains("successfully");
    }

    @Test
    void processOrder_validationFailure() {
        // Use activities where validation fails
        worker.registerActivitiesImplementations(
                new FailingValidationActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build());

        assertThatThrownBy(() -> workflow.processOrder("ORD-123", "CUST-456", -100L))
                .isInstanceOf(WorkflowFailedException.class)
                .hasMessageContaining("Invalid amount");
    }

    @Test
    void processOrder_paymentFailure() {
        // Use activities where payment fails
        worker.registerActivitiesImplementations(
                new SuccessOrderActivities(),
                new FailingPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build());

        assertThatThrownBy(() -> workflow.processOrder("ORD-123", "CUST-456", 1000L))
                .isInstanceOf(WorkflowFailedException.class)
                .hasMessageContaining("Payment gateway unavailable");
    }

    @Test
    void processOrder_inventoryUnavailable() {
        // Use activities where inventory is unavailable
        worker.registerActivitiesImplementations(
                new NoInventoryActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build());

        assertThatThrownBy(() -> workflow.processOrder("ORD-123", "CUST-456", 1000L))
                .isInstanceOf(WorkflowFailedException.class)
                .hasMessageContaining("Insufficient inventory");
    }

    @Test
    void processOrder_queryStatus() {
        worker.registerActivitiesImplementations(
                new SlowOrderActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build());

        // Start workflow async
        WorkflowClient.start(workflow::processOrder, "ORD-123", "CUST-456", 1000L);

        // Query status - should be processing
        var status = workflow.getStatus();
        assertThat(status).isNotNull();
    }

    // --- Stubbed Activity Implementations ---

    /**
     * Activities that always succeed.
     */
    public static class SuccessOrderActivities implements OrderActivities {
        @Override
        public void validateOrder(String orderId, long amount) {
            // Success
        }

        @Override
        public boolean checkInventory(String orderId, int quantity) {
            return true;
        }

        @Override
        public void reserveInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void releaseInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void sendNotification(String customerId, String message) {
            // Success
        }
    }

    /**
     * Activities where validation fails.
     */
    public static class FailingValidationActivities implements OrderActivities {
        @Override
        public void validateOrder(String orderId, long amount) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        @Override
        public boolean checkInventory(String orderId, int quantity) {
            return true;
        }

        @Override
        public void reserveInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void releaseInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void sendNotification(String customerId, String message) {
            // Success
        }
    }

    /**
     * Activities where inventory is unavailable.
     */
    public static class NoInventoryActivities implements OrderActivities {
        @Override
        public void validateOrder(String orderId, long amount) {
            // Success
        }

        @Override
        public boolean checkInventory(String orderId, int quantity) {
            return false; // No inventory
        }

        @Override
        public void reserveInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void releaseInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void sendNotification(String customerId, String message) {
            // Success
        }
    }

    /**
     * Slow activities for testing queries during execution.
     */
    public static class SlowOrderActivities implements OrderActivities {
        @Override
        public void validateOrder(String orderId, long amount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean checkInventory(String orderId, int quantity) {
            return true;
        }

        @Override
        public void reserveInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void releaseInventory(String orderId, int quantity) {
            // Success
        }

        @Override
        public void sendNotification(String customerId, String message) {
            // Success
        }
    }

    /**
     * Payment activities that always succeed.
     */
    public static class SuccessPaymentActivities implements PaymentActivities {
        @Override
        public String authorizePayment(String customerId, long amount) {
            return "AUTH-12345678";
        }

        @Override
        public void capturePayment(String authorizationId, long amount) {
            // Success
        }

        @Override
        public void refundPayment(String authorizationId, long amount) {
            // Success
        }
    }

    /**
     * Payment activities that fail authorization.
     */
    public static class FailingPaymentActivities implements PaymentActivities {
        @Override
        public String authorizePayment(String customerId, long amount) {
            throw new RuntimeException("Payment gateway unavailable");
        }

        @Override
        public void capturePayment(String authorizationId, long amount) {
            // Success
        }

        @Override
        public void refundPayment(String authorizationId, long amount) {
            // Success
        }
    }
}
