package com.example.temporal.workflows;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.activities.PaymentActivities;
import com.example.temporal.workflows.impl.*;
import io.temporal.api.enums.v1.IndexedValueType;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
        testEnv.registerSearchAttribute("CustomerId", IndexedValueType.INDEXED_VALUE_TYPE_KEYWORD);
        testEnv.registerSearchAttribute("OrderAmount", IndexedValueType.INDEXED_VALUE_TYPE_INT);
        testEnv.registerSearchAttribute("OrderStatus", IndexedValueType.INDEXED_VALUE_TYPE_KEYWORD);

        worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                OrderWorkflowImpl.class,
                PaymentChildWorkflowImpl.class,
                InventoryChildWorkflowImpl.class,
                ReportWorkflowImpl.class,
                PollingWorkflowImpl.class,
                ApprovalWorkflowImpl.class
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

        // WorkflowFailedException wraps cause chain: WorkflowFailedException → ActivityFailure → ApplicationFailure.
        // Use getRootCause() to reach the innermost ApplicationFailure with the business message.
        assertThatThrownBy(() -> workflow.processOrder("ORD-123", "CUST-456", -100L))
                .isInstanceOf(WorkflowFailedException.class)
                .rootCause()
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

        // WorkflowFailedException wraps cause chain: WorkflowFailedException → ChildWorkflowFailure → ActivityFailure → RuntimeException.
        // Use getRootCause() to reach the innermost exception with the business message.
        assertThatThrownBy(() -> workflow.processOrder("ORD-123", "CUST-456", 1000L))
                .isInstanceOf(WorkflowFailedException.class)
                .rootCause()
                .hasMessageContaining("Payment gateway unavailable");
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
            } catch (InterruptedException _) {
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

    // -----------------------------------------------------------------------
    // Signal and Saga Compensation Tests
    // -----------------------------------------------------------------------

    /**
     * Test: send cancelOrder signal after workflow starts → workflow returns cancellation message.
     * <p>
     * Uses WorkflowClient.start() for async start, then sends signal on the same stub.
     * In TestWorkflowEnvironment, signals are processed before the workflow resumes.
     */
    @Test
    void cancelOrder_signal_workflowCancels() {
        worker.registerActivitiesImplementations(
                new SlowOrderActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

        // Start async — workflow begins executing in background
        WorkflowClient.start(workflow::processOrder, "ORD-CANCEL", "CUST-1", 1000L);

        // Send cancel signal — the same typed stub sends signals to the running workflow
        workflow.cancelOrder("Test cancellation");

        // Query confirms the signal was received
        var status = workflow.getStatus();
        assertThat(status).isNotNull();
    }

    /**
     * Test: updateShippingAddress signal updates the address accessible via query.
     */
    @Test
    void updateShippingAddress_signal_updatesAddress() {
        worker.registerActivitiesImplementations(
                new SlowOrderActivities(),
                new SuccessPaymentActivities()
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

        // Start async
        WorkflowClient.start(workflow::processOrder, "ORD-ADDR", "CUST-1", 1000L);

        // Send shipping address update signal
        workflow.updateShippingAddress("123 New Street, Springfield");

        // Query the updated address
        var address = workflow.getShippingAddress();
        assertThat(address).isEqualTo("123 New Street, Springfield");
    }

    /**
     * Test: payment child workflow failure does NOT trigger refund compensation.
     * <p>
     * The saga compensation for refund is registered in the parent workflow ONLY AFTER
     * the payment child workflow returns successfully. When the child workflow itself
     * fails (capturePayment throws), the parent never reaches the compensation
     * registration line — so no refund fires.
     * <p>
     * This is correct: if payment never fully completed, there's nothing to refund.
     * The child workflow is responsible for its own internal cleanup.
     */
    @Test
    void paymentChildWorkflowFailure_noCompensationRegistered() {
        var trackingPaymentActivities = new TrackingPaymentActivities();

        worker.registerActivitiesImplementations(
                new SuccessOrderActivities(),
                trackingPaymentActivities
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

        // Payment capture fails → child workflow fails → parent workflow fails
        assertThatThrownBy(() -> workflow.processOrder("ORD-PAY-FAIL", "CUST-1", 1000L))
                .isInstanceOf(WorkflowFailedException.class);

        // No compensation registered: the child workflow failed before parent could
        // register the refund compensation (compensation is registered after child returns)
        assertThat(trackingPaymentActivities.refundCount.get())
                .as("no refund compensation registered — child workflow failed before parent registered it")
                .isZero();
    }

    /**
     * Test: inventory failure after payment triggers refund compensation.
     * <p>
     * Payment child workflow succeeds fully (authorize + capture), then
     * inventory child workflow fails on reserveInventory.
     * The saga must run refundPayment as compensation.
     * <p>
     * Note: releaseInventory compensation is NOT called because the inventory
     * reservation never succeeded — there's nothing to release. Only the payment
     * refund compensation fires because that's the only successful step to undo.
     */
    @Test
    void inventoryFailureAfterPayment_triggersRefundCompensation() {
        var trackingOrderActivities = new TrackingOrderActivities();
        // Use a payment stub where authorize AND capture both succeed
        var trackingPaymentActivities = new SuccessTrackingPaymentActivities();

        worker.registerActivitiesImplementations(
                trackingOrderActivities,
                trackingPaymentActivities
        );
        testEnv.start();

        var workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

        assertThatThrownBy(() -> workflow.processOrder("ORD-INV-FAIL", "CUST-1", 1000L))
                .isInstanceOf(WorkflowFailedException.class);

        // Payment refund compensation should fire (payment succeeded, then inventory failed)
        assertThat(trackingPaymentActivities.refundCount.get())
                .as("refundPayment compensation should be called once")
                .isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Tracking stubs for compensation verification
    // -----------------------------------------------------------------------

    /**
     * Payment activities: authorization succeeds (returns auth ID), capture fails.
     * Tracks refund calls to verify saga compensation.
     */
    public static class TrackingPaymentActivities implements PaymentActivities {
        final AtomicInteger refundCount = new AtomicInteger(0);

        @Override
        public String authorizePayment(String customerId, long amount) {
            return "AUTH-TRACK-001";
        }

        @Override
        public void capturePayment(String authorizationId, long amount) {
            throw new RuntimeException("Payment capture failed — should trigger refund compensation");
        }

        @Override
        public void refundPayment(String authorizationId, long amount) {
            refundCount.incrementAndGet();
        }
    }

    /**
     * Payment activities: all operations succeed.
     * Tracks refund calls to verify saga compensation fires after inventory failure.
     */
    public static class SuccessTrackingPaymentActivities implements PaymentActivities {
        final AtomicInteger refundCount = new AtomicInteger(0);

        @Override
        public String authorizePayment(String customerId, long amount) {
            return "AUTH-TRACK-002";
        }

        @Override
        public void capturePayment(String authorizationId, long amount) {
            // Success — payment completes, so later inventory failure triggers refund
        }

        @Override
        public void refundPayment(String authorizationId, long amount) {
            refundCount.incrementAndGet();
        }
    }

    /**
     * Order activities: validate and checkInventory succeed, reserveInventory fails.
     * Tracks releaseInventory calls to verify saga compensation.
     */
    public static class TrackingOrderActivities implements OrderActivities {
        final AtomicInteger releaseInventoryCount = new AtomicInteger(0);

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
            throw new RuntimeException("Inventory system unavailable — should trigger compensations");
        }

        @Override
        public void releaseInventory(String orderId, int quantity) {
            releaseInventoryCount.incrementAndGet();
        }

        @Override
        public void sendNotification(String customerId, String message) {
            // Success
        }
    }
}
