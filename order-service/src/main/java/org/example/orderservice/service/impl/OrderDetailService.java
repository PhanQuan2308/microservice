package org.example.orderservice.service.impl;

import org.example.orderservice.entity.OrderDetail;
import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> getOrderDetailByOrderId(Long orderId);
    void deleteOrderDetailByOrderId(Long orderId);
}
