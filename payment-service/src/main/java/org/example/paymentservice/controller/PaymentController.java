package org.example.paymentservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.paymentservice.dto.request.PaymentRequestDTO;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.response.ApiResponse;
import org.example.paymentservice.service.PayPalService;
import org.example.paymentservice.service.impl.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentServiceImpl paymentService;
    private final PayPalService payPalService;

    @Autowired
    public PaymentController(PaymentServiceImpl paymentService, PayPalService payPalService) {
        this.paymentService = paymentService;
        this.payPalService = payPalService;
    }

    @PutMapping("/update-status/{orderId}")
    public ResponseEntity<ApiResponse<Void>> updatePaymentStatusByOrderId(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            paymentService.updatePaymentStatusByOrderId(orderId, status);

            ApiResponse<Void> response = ApiResponse.success(null, "Payment status updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error in Controller: " + e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Failed to update payment status: " + e.getMessage(),
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/paypal")
    public ResponseEntity<Map<String, String>> createPayPalPayment(@RequestParam Double amount) {
        if (amount == null || amount <= 0) {
            System.err.println("Invalid amount provided: " + amount);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        System.out.println("Received request to create PayPal payment for amount: " + amount);
        try {
            Map<String, String> paymentResult = payPalService.createPayment(amount);
            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            System.err.println("Error in createPayPalPayment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyPayment(@RequestParam String token,
                                                 @RequestParam(required = false) Double amount) {
        if (amount == null) {
            System.err.println("Amount is null. Cannot verify payment.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }

        boolean isVerified = payPalService.verifyPaymentStatus(token, amount);
        if (isVerified) {
            System.out.println("Payment verified successfully for token: " + token);
            return ResponseEntity.ok(true);
        } else {
            System.err.println("Payment verification failed for token: " + token);
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/paypal-return")
    public void handlePayPalReturn(@RequestParam String token,
                                   @RequestParam(required = false) Double amount,
                                   HttpServletResponse response) throws IOException {
        System.out.println("PayPal Return with token: " + token);
        System.out.println("Received amount: " + amount);

        if (amount == null) {
            System.err.println("Error: Amount is null. Cannot verify payment.");
            response.sendRedirect("http://localhost:4200/user/checkout?token=" + token + "&status=failed");
            return;
        }

        boolean isVerified = payPalService.verifyPaymentStatus(token, amount);
        if (isVerified) {
            response.sendRedirect("http://localhost:4200/user/checkout?token=" + token + "&status=success");
        } else {
            response.sendRedirect("http://localhost:4200/user/checkout?token=" + token + "&status=failed");
        }
    }





    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        Payment payment = paymentService.createPayment(paymentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }


    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}