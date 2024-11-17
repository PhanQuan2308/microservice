package org.example.orderservice.service;

import org.example.orderservice.dto.AddressDTO;
import org.example.orderservice.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrder(Long orderId, OrderDTO updatedOrderDTO);
    void deleteOrder(Long orderId);
    OrderDTO getOrderById(Long orderId);
    List<OrderDTO> getAllOrders();
    AddressDTO updateAddress(Long orderId, AddressDTO updatedAddressDTO);
    void handlePaymentCallback(String token, boolean isPaymentSuccessful);
    String initiatePayment(OrderDTO orderDTO);
}
