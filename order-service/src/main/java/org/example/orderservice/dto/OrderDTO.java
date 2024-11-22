package org.example.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderDTO {

    private Long orderId;

    private Long userId;

    private Date orderDate = new Date();

    private String status;

    @Positive(message = "Shipping fee must be greater than 0")
    private Double shippingFee;


    private Double totalAmount;

    @NotEmpty(message = "Order details cannot be empty")
    private List<OrderDetailsDTO> orderDetails;

    private String paymentUrl;

    private String paymentToken;
    private String transactionId;

    @NotNull(message = "Address cannot be null")
    private AddressDTO address;
}