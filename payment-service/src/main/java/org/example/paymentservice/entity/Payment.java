package org.example.paymentservice.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long orderId;

    private Date paymentDate;
    private Double amount;

    private String status;

    private String transactionId;
    private String vnpayStatus;
    private String vnpayMessage;






}
