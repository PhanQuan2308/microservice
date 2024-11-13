    package org.example.orderservice.client;

    import org.example.orderservice.dto.PaymentRequestDTO;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestParam;

    @FeignClient(name = "payment-service")
    public interface PaymentServiceClient {
        @GetMapping("api/v1/payments/paypal")
        String createPayPalPayment(@RequestParam Double amount);

        @GetMapping("api/v1/payments/verify")
        Boolean verifyPayment(@RequestParam String token, @RequestParam Double amount);

        @PostMapping("api/v1/payments")
        void createPayment(@RequestBody PaymentRequestDTO paymentRequestDTO);

    }
