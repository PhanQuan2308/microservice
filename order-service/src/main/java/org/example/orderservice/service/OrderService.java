package org.example.orderservice.service;

import org.example.orderservice.dto.AddressDTO;
import org.example.orderservice.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrder(Long orderId, OrderDTO updatedOrderDTO);
    void deleteOrder(Long orderId);
    OrderDTO getOrderById(Long orderId);
    Page<OrderDTO> getAllOrders(Pageable pageable);
    AddressDTO updateAddress(Long orderId, AddressDTO updatedAddressDTO);
    Long handlePaymentCallback(String token, boolean isPaymentSuccessful);
    String initiatePayment(OrderDTO orderDTO);
}
