package org.example.orderservice.client;

import org.example.orderservice.dto.PaymentRequestDTO;
import org.example.orderservice.dto.ProductDTO;
import org.example.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    @GetMapping("api/v1/payments/paypal")
    Map<String, String> createPayPalPayment(@RequestParam Double amount);


    @GetMapping("api/v1/payments/verify")
    Boolean verifyPayment(@RequestParam String token, @RequestParam Double amount);

    @PostMapping("api/v1/payments")
    void createPayment(@RequestBody PaymentRequestDTO paymentRequestDTO);

    @PutMapping("/api/v1/payments/update-status/{orderId}")
    void updatePaymentStatusByOrderId(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);


}