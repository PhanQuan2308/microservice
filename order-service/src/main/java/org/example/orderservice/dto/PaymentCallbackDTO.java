package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCallbackDTO {
    private String token;
    private boolean isPaymentSuccessful;

    public boolean getIsPaymentSuccessful() {
        return isPaymentSuccessful;
    }

}
