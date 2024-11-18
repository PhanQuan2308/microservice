package org.example.orderservice.controller;

import org.example.orderservice.client.PaymentServiceClient;
import org.example.orderservice.dto.AddressDTO;
import org.example.orderservice.dto.OrderDTO;

import org.example.orderservice.dto.PaymentCallbackDTO;
import org.example.orderservice.service.impl.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            orderDTO.setUserId(Long.parseLong(userId));

            OrderDTO createdOrder = orderService.createOrder(orderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/payment-callback")
    public ResponseEntity<Map<String, Object>> handlePaymentCallback(@RequestBody PaymentCallbackDTO paymentCallbackDTO) {
        logger.info("Received callback: {}", paymentCallbackDTO);
        String token = paymentCallbackDTO.getToken();
        Boolean isPaymentSuccessful = paymentCallbackDTO.getIsPaymentSuccessful();

        try {
            Long orderId = orderService.handlePaymentCallback(token, isPaymentSuccessful);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("message", "Payment callback handled successfully.");

            logger.info("Payment callback handled for orderId: {}", orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in payment callback: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error handling payment callback");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }





    @GetMapping("/getall")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderDTO updatedOrderDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(orderId, updatedOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orderId}/address")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long orderId, @RequestBody AddressDTO updatedAddressDTO) {
        AddressDTO updatedAddress = orderService.updateAddress(orderId, updatedAddressDTO);
        return ResponseEntity.ok(updatedAddress);
    }

}
