package org.example.paymentservice.service.impl;

import org.example.paymentservice.dto.PaymentRequestDTO;
import org.example.paymentservice.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment createPayment(PaymentRequestDTO paymentRequestDTO);
    List<Payment> getAllPayments();
    Optional<Payment> getPaymentById(Long paymentId);
    Payment updatePaymentStatus(Long paymentId, String status);
    void deletePayment(Long paymentId);

}
