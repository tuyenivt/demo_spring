package com.example.websocket.exception;

import com.example.websocket.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.Instant;

import static com.example.websocket.constant.WebSocketDestinations.QUEUE_ERRORS;

/**
 * Global exception handler for WebSocket messages.
 * <p>
 * Catches exceptions thrown during message processing and sends
 * error responses back to the client.
 */
@Slf4j
@ControllerAdvice
public class WebSocketErrorHandler {

    /**
     * Handle generic exceptions during message processing.
     *
     * @SendToUser sends the error only to the user who triggered it
     * Uses /queue/errors destination for point-to-point error delivery
     */
    @MessageExceptionHandler
    @SendToUser(QUEUE_ERRORS)
    public ErrorResponse handleException(Exception exception) {
        log.error("Error processing WebSocket message", exception);

        var exceptionMessage = exception.getMessage() != null ? exception.getMessage() : "An error occurred";
        return new ErrorResponse("MESSAGE_PROCESSING_ERROR", exceptionMessage, Instant.now());
    }

    /**
     * Handle illegal argument exceptions (validation failures).
     */
    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser(QUEUE_ERRORS)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Validation error: {}", exception.getMessage());

        return new ErrorResponse("VALIDATION_ERROR", exception.getMessage(), Instant.now());
    }
}
