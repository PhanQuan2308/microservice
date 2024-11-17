package org.example.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentVerificationException(PaymentVerificationException ex) {
        return buildResponse(HttpStatus.PAYMENT_REQUIRED, "Payment Verification Failed", ex.getMessage());
    }

    @ExceptionHandler(StockAdjustmentException.class)
    public ResponseEntity<Map<String, Object>> handleStockAdjustmentException(StockAdjustmentException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Stock Adjustment Failed", ex.getMessage());
    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCreationException(OrderCreationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Order Creation Failed", ex.getMessage());
    }

    // Method to build consistent response
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
