package org.example.orderservice.service;

import org.example.orderservice.client.ProductServiceClient;
import org.example.orderservice.dto.AddressDTO;
import org.example.orderservice.dto.OrderDTO;
import org.example.orderservice.dto.OrderDetailsDTO;
import org.example.orderservice.dto.ProductDTO;
import org.example.orderservice.entity.Address;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderDetail;
import org.example.orderservice.producer.OrderProducer;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.OrderDetailRepository;
import org.example.orderservice.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private OrderProducer orderProducer;


    @Autowired
    private RestTemplate restTemplate;

    private Address convertToEntity(AddressDTO addressDTO) {
        Address address = new Address();
        address.setRecipientName(addressDTO.getRecipientName());
        address.setRecipientAddress(addressDTO.getRecipientAddress());
        address.setRecipientPhone(addressDTO.getRecipientPhone());
        return address;
    }
    private AddressDTO convertToDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setRecipientName(address.getRecipientName());
        addressDTO.setRecipientAddress(address.getRecipientAddress());
        addressDTO.setRecipientPhone(address.getRecipientPhone());
        return addressDTO;
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setShippingFee(order.getShippingFee());
        orderDTO.setAddress(convertToDTO(order.getAddress()));

        List<OrderDetailsDTO> orderDetailDTOs = order.getOrderDetails().stream().map(detail -> {
            OrderDetailsDTO detailsDTO = new OrderDetailsDTO();
            detailsDTO.setId(detail.getId());
            detailsDTO.setProductId(detail.getProductId());
            detailsDTO.setQuantity(detail.getQuantity());
            detailsDTO.setPrice(detail.getPrice());

            try {
                ProductDTO productDTO = productServiceClient.getProductById(detail.getProductId());
                detailsDTO.setProductName(productDTO.getProductName());
                detailsDTO.setProduct(productDTO);
            } catch (RuntimeException e) {
                System.err.println("Product not found for productId " + detail.getProductId() + ": " + e.getMessage());
                detailsDTO.setProductName("Unknown Product");
                detailsDTO.setProduct(null);
            }

            return detailsDTO;
        }).collect(Collectors.toList());

        orderDTO.setOrderDetails(orderDetailDTOs);
        return orderDTO;
    }


    private Order convertToOrderEntity(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderId(orderDTO.getOrderId());
        order.setUserId(orderDTO.getUserId());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setStatus(orderDTO.getStatus());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setShippingFee(orderDTO.getShippingFee());
        Address address = convertToEntity(orderDTO.getAddress());
        order.setAddress(address);
        if (address != null) {
            address.setOrder(order);
        }

        List<OrderDetail> orderDetails = orderDTO.getOrderDetails().stream().map(detailDTO -> {
            OrderDetail detail = new OrderDetail();
            detail.setProductId(detailDTO.getProductId());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setPrice(detailDTO.getPrice());
            detail.setOrder(order);
            return detail;
        }).collect(Collectors.toList());

        order.setOrderDetails(orderDetails);
        return order;
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = convertToOrderEntity(orderDTO);
        order.setStatus("PENDING");
        if (order.getShippingFee() == null) {
            order.setShippingFee(10.0);
        }
        double subtotal = orderDTO.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
        double totalAmount = subtotal + order.getShippingFee();
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        orderDetailRepository.saveAll(order.getOrderDetails());

        double amount = orderDTO.getTotalAmount();
        String paymentServiceUrl = "http://localhost:8080/api/v1/payments/paypal?amount=" + amount;
        String paymentUrl = restTemplate.getForObject(paymentServiceUrl, String.class);

        OrderDTO createdOrderDTO = convertToOrderDTO(savedOrder);
        createdOrderDTO.setPaymentUrl(paymentUrl);
        return createdOrderDTO;
    }




    @Override
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO updatedOrderDTO) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (updatedOrderDTO.getStatus() != null) {
            existingOrder.setStatus(updatedOrderDTO.getStatus());
        }
        if (updatedOrderDTO.getTotalAmount() != null) {
            existingOrder.setTotalAmount(updatedOrderDTO.getTotalAmount());
        }
        if (updatedOrderDTO.getUserId() != null) {
            existingOrder.setUserId(updatedOrderDTO.getUserId());
        }

        if (updatedOrderDTO.getOrderDetails() != null) {
            orderDetailRepository.deleteByOrder_OrderId(orderId);

            List<OrderDetail> updatedOrderDetails = updatedOrderDTO.getOrderDetails().stream().map(detailDTO -> {
                OrderDetail detail = new OrderDetail();
                detail.setProductId(detailDTO.getProductId());
                detail.setQuantity(detailDTO.getQuantity());
                detail.setPrice(detailDTO.getPrice());
                detail.setOrder(existingOrder);
                return detail;
            }).collect(Collectors.toList());

            existingOrder.setOrderDetails(updatedOrderDetails);
            orderDetailRepository.saveAll(updatedOrderDetails);
        }

        return convertToOrderDTO(orderRepository.save(existingOrder));
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        orderDetailRepository.deleteByOrder_OrderId(orderId);
        orderRepository.deleteById(orderId);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToOrderDTO(order);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }
}
