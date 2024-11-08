package org.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    private String recipientName;
    private String recipientAddress;
    private String recipientPhone;

    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL)
    private Order order;
}
