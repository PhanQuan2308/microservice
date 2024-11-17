package org.example.orderservice.exception;

public class StockAdjustmentException extends RuntimeException {
    public StockAdjustmentException(String message) {
        super(message);
    }
}
