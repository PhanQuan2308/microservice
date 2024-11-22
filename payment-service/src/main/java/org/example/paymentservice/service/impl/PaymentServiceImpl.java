package org.example.paymentservice.service.impl;

import org.example.paymentservice.dto.request.PaymentRequestDTO;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.service.PayPalService;
import org.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {



    private final PaymentRepository paymentRepository;

    private final PayPalService payPalService;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PayPalService payPalService) {
        this.paymentRepository = paymentRepository;
        this.payPalService = payPalService;
    }


    @Override
    public Payment createPayment(PaymentRequestDTO paymentRequestDTO) {
        Payment payment = new Payment();
        payment.setOrderId(paymentRequestDTO.getOrderId());
        payment.setAmount(paymentRequestDTO.getAmount());
        payment.setStatus(paymentRequestDTO.getStatus());
        payment.setUserId(paymentRequestDTO.getUserId());
        payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
        payment.setTransactionId(paymentRequestDTO.getTransactionId());
        payment.setPaymentDate(new Date());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void updatePaymentStatusByOrderId(Long orderId, String status) {
        try {
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for Order ID: " + orderId));
            payment.setStatus(status);
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, String status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            return paymentRepository.save(payment);
        } else {
            throw new RuntimeException("Payment not found");
        }
    }

    @Override
    public void deletePayment(Long paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    public String createPayPalPayment(Double amount) throws Exception {
        Map<String, String> paymentResult = payPalService.createPayment(amount);
        return paymentResult.get("approvalLink");
    }

}