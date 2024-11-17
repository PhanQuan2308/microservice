package org.example.orderservice.dto;

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
    private Double shippingFee;
    private Double totalAmount;
    private List<OrderDetailsDTO> orderDetails;
    private String paymentUrl;
    private String paymentToken;

    private AddressDTO address;
}
