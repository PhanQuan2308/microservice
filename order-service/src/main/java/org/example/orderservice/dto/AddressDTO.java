package org.example.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    private Long addressId;

    @NotNull(message = "Recipient name is required")
    private String recipientName;

    @NotNull(message = "Recipient phone is required")
    private String recipientPhone;

    @NotNull(message = "Recipient email is required")
    private String recipientEmail;

    @NotNull(message = "Recipient address is required")
    private String recipientAddress;

    @NotNull(message = "Recipient city is required")
    private String recipientCity;

    @NotNull(message = "Recipient country is required")
    private String recipientCountry;
}
