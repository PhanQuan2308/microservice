package org.example.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    private Long addressId;
    private String recipientName;
    private String recipientAddress;
    private String recipientPhone;

}
