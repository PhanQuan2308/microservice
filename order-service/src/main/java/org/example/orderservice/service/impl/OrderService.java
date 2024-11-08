package org.example.orderservice.service.impl;

import org.example.orderservice.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO   createOrder(OrderDTO orderDTO);
    OrderDTO updateOrder(Long orderId, OrderDTO updatedOrderDTO);
    void deleteOrder(Long orderId);
    OrderDTO getOrderById(Long orderId);
    List<OrderDTO> getAllOrders();
}
