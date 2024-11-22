package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {
    private Long orderId;
    private Double amount;
    private String status;
    private Long userId;
    private String paymentMethod;
    private String transactionId;
}