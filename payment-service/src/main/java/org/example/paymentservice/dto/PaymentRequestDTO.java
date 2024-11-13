package org.example.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentRequestDTO {
    private Long orderId;
    private Double amount;
    private String status;
    private Long userId;
    private String paymentMethod;
}
