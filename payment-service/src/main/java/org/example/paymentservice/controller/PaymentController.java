package org.example.paymentservice.controller;

import org.example.paymentservice.dto.PaymentRequestDTO;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.service.PayPalService;
import org.example.paymentservice.service.impl.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/paypal")
    public ResponseEntity<String> createPayPalPayment(@RequestParam Double amount) {
        System.out.println("Received request to create PayPal payment for amount: " + amount);
        try {
            String paymentUrl = payPalService.createPayment(amount);
            System.out.println("Redirecting to PayPal approval URL: " + paymentUrl);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            System.err.println("Error in createPayPalPayment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating PayPal payment");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyPayment(@RequestParam String token,@RequestParam Double amount) {
        boolean isVerified = payPalService.verifyPaymentStatus(token, amount);
        if (isVerified) {
            return ResponseEntity.ok(true);
        }else {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/paypal-return")
    public ResponseEntity<String> handlePayPalReturn(@RequestParam String token) {
        System.out.println("PayPal Return with token: " + token);
        return ResponseEntity.ok("Payment Successful");
    }

    @GetMapping("/paypal-cancel")
    public ResponseEntity<String> handlePayPalCancel() {
        System.out.println("PayPal Payment Canceled");
        return ResponseEntity.ok("Payment Canceled");
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
