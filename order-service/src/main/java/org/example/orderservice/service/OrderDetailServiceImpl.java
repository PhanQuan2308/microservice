package org.example.orderservice.service;

import org.example.orderservice.entity.OrderDetail;
import org.example.orderservice.repository.OrderDetailRepository;
import org.example.orderservice.service.impl.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetail> getOrderDetailByOrderId(Long orderId) {
        return orderDetailRepository.findByOrder_OrderId(orderId);
    }

    @Override
    public void deleteOrderDetailByOrderId(Long orderId) {
        orderDetailRepository.deleteByOrder_OrderId(orderId);
    }
}