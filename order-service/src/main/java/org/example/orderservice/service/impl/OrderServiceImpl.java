package org.example.orderservice.service.impl;

import org.example.orderservice.client.PaymentServiceClient;
import org.example.orderservice.client.ProductServiceClient;
import org.example.orderservice.dto.*;
import org.example.orderservice.entity.Address;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderDetail;
import org.example.orderservice.event.OrderEvent;
import org.example.orderservice.event.ProductStockReductionRequest;
import org.example.orderservice.producer.OrderProducer;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.OrderDetailRepository;
import org.example.orderservice.response.ApiResponse;
import org.example.orderservice.service.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import java.util.List;
import java.util.Map;
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
    private PaymentServiceClient paymentServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    private Address convertToEntity(AddressDTO addressDTO) {
        Address address = new Address();
        address.setAddressId(addressDTO.getAddressId());
        address.setRecipientName(addressDTO.getRecipientName());
        address.setRecipientPhone(addressDTO.getRecipientPhone());
        address.setRecipientEmail(addressDTO.getRecipientEmail());
        address.setRecipientAddress(addressDTO.getRecipientAddress());
        address.setRecipientCity(addressDTO.getRecipientCity());
        address.setRecipientCountry(addressDTO.getRecipientCountry());
        return address;
    }
    private AddressDTO convertToDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(address.getAddressId());
        addressDTO.setRecipientName(address.getRecipientName());
        addressDTO.setRecipientPhone(address.getRecipientPhone());
        addressDTO.setRecipientEmail(address.getRecipientEmail());
        addressDTO.setRecipientAddress(address.getRecipientAddress());
        addressDTO.setRecipientCity(address.getRecipientCity());
        addressDTO.setRecipientCountry(address.getRecipientCountry());
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
        orderDTO.setPaymentToken(order.getPaymentToken());
        orderDTO.setTransactionId(order.getTransactionId());
        orderDTO.setAddress(convertToDTO(order.getAddress()));

        List<OrderDetailsDTO> orderDetailDTOs = order.getOrderDetails().stream().map(detail -> {
            OrderDetailsDTO detailsDTO = new OrderDetailsDTO();
            detailsDTO.setId(detail.getId());
            detailsDTO.setProductId(detail.getProductId());
            detailsDTO.setProductName(detail.getProductName());
            detailsDTO.setQuantity(detail.getQuantity());
            detailsDTO.setPrice(detail.getPrice());

            try {
                // Gọi ProductServiceClient
                ApiResponse<ProductDTO> response = productServiceClient.getProductById(detail.getProductId());
                if (response != null && response.getData() != null) {
                    ProductDTO productDTO = response.getData();
                    detailsDTO.setProductName(productDTO.getProductName());
                    detailsDTO.setProduct(productDTO);
                } else {
                    logger.warn("No product details found for productId: {}", detail.getProductId());
                    detailsDTO.setProductName("Unknown Product");
                    detailsDTO.setProduct(null);
                }
            } catch (Exception e) {
                logger.error("Error fetching product details for productId: {}", detail.getProductId(), e);
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
        order.setTransactionId(orderDTO.getTransactionId());
        Address address = convertToEntity(orderDTO.getAddress());
        order.setAddress(address);
        if (address != null) {
            address.setOrder(order);
        }

        List<OrderDetail> orderDetails = orderDTO.getOrderDetails().stream().map(detailDTO -> {
            logger.info("Converting OrderDetail with productId: {}", detailDTO.getProductId());
            OrderDetail detail = new OrderDetail();
            detail.setProductId(detailDTO.getProductId());
            detail.setProductName(detailDTO.getProductName());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setPrice(detailDTO.getPrice());
            detail.setOrder(order);
            return detail;
        }).collect(Collectors.toList());

        order.setOrderDetails(orderDetails);
        return order;
    }



    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = convertToOrderEntity(orderDTO);
        order.setStatus("PENDING");

        double subtotal = orderDTO.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
        double totalAmount = subtotal + order.getShippingFee();
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        orderDetailRepository.saveAll(order.getOrderDetails());

        try {
            Map<String, String> paymentResult = paymentServiceClient.createPayPalPayment(totalAmount);
            String paymentUrl = paymentResult.get("approvalLink");
            String transactionId = paymentResult.get("transactionId");

            savedOrder.setPaymentToken(extractTokenFromUrl(paymentUrl));
            savedOrder.setTransactionId(transactionId);
            orderRepository.save(savedOrder);

            PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
            paymentRequestDTO.setOrderId(savedOrder.getOrderId());
            paymentRequestDTO.setAmount(totalAmount);
            paymentRequestDTO.setStatus("PENDING");
            paymentRequestDTO.setUserId(order.getUserId());
            paymentRequestDTO.setPaymentMethod("PAYPAL");
            paymentRequestDTO.setTransactionId(transactionId);

            paymentServiceClient.createPayment(paymentRequestDTO);
            logger.info("Payment created with status PENDING for Order ID: {}", savedOrder.getOrderId());

            emailService.sendEmail(
                    orderDTO.getAddress().getRecipientEmail(),
                    "Order Created Successfully",
                    "Your order has been created successfully. Total amount: " + totalAmount
            );

            OrderDTO responseDTO = convertToOrderDTO(savedOrder);
            responseDTO.setPaymentUrl(paymentUrl);
            return responseDTO;
        } catch (Exception e) {
            savedOrder.setStatus("FAILED");
            orderRepository.save(savedOrder);
            throw new RuntimeException("Failed to initiate payment for Order ID: " + savedOrder.getOrderId());
        }
    }


    private String extractTokenFromUrl(String paymentUrl) {
        if (paymentUrl != null && paymentUrl.contains("token=")) {
            String[] parts = paymentUrl.split("token=");
            if (parts.length > 1) {
                String token = parts[1].split("&")[0];
                return token;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }



    @Override
    @Transactional
    public Long handlePaymentCallback(String token, boolean isPaymentSuccessful) {
        Order order = orderRepository.findByPaymentToken(token)
                .orElseThrow(() -> new RuntimeException("Order not found for token: " + token));

        try {
            if (isPaymentSuccessful) {
                order.setStatus("COMPLETED");
                orderRepository.save(order);

                try {
                    List<ProductStockReductionRequest> stockReductions = order.getOrderDetails().stream()
                            .map(detail -> new ProductStockReductionRequest(detail.getProductId(), detail.getQuantity()))
                            .collect(Collectors.toList());

                    OrderEvent orderEvent = new OrderEvent(order.getOrderId(), stockReductions);
                    orderProducer.sendOrderEvent(orderEvent);
                    logger.info("Order event sent to Kafka: {}", orderEvent);

                } catch (Exception e) {
                    logger.error("Failed to send order event to Kafka", e);
                    order.setStatus("PENDING");
                    orderRepository.save(order);

                    emailService.sendEmail(
                            order.getAddress().getRecipientEmail(),
                            "Payment Successful but Stock Update Failed",
                            "Your payment for Order ID " + order.getOrderId() + " has been completed successfully, but we encountered an issue while updating the stock. Our team is investigating the issue. Please try again later."
                    );

                    throw new RuntimeException("Failed to reduce stock for Order ID: " + order.getOrderId() + " due to Kafka error.");
                }

                emailService.sendEmail(
                        order.getAddress().getRecipientEmail(),
                        "Payment Successful",
                        "Your payment for Order ID " + order.getOrderId() + " has been completed successfully."
                );

            } else {
                order.setStatus("FAILED");
                orderRepository.save(order);
                emailService.sendEmail(
                        order.getAddress().getRecipientEmail(),
                        "Payment Failed",
                        "Your payment for Order ID " + order.getOrderId() + " has failed. Please try again."
                );
            }

        } catch (Exception e) {
            logger.error("Error handling payment callback: ", e);
            throw new RuntimeException("Failed to handle payment callback for Order ID: " + order.getOrderId(), e);
        }

        return order.getOrderId();
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderDTO updatedOrderDTO) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));



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
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAll(pageable);
        List<OrderDTO> orderDTOs = ordersPage.getContent().stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(orderDTOs, pageable, ordersPage.getTotalElements());
    }


    @Override
    @Transactional
    public AddressDTO updateAddress(Long orderId, AddressDTO updatedAddressDTO) {
        Order existOrderId = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Address existingAddress = existOrderId.getAddress();
        if(existingAddress == null){
            existingAddress = new Address();
            existOrderId.setAddress(existingAddress);
        }

        existingAddress.setRecipientName(updatedAddressDTO.getRecipientName());
        existingAddress.setRecipientPhone(updatedAddressDTO.getRecipientPhone());
        existingAddress.setRecipientEmail(updatedAddressDTO.getRecipientEmail());
        existingAddress.setRecipientAddress(updatedAddressDTO.getRecipientAddress());
        existingAddress.setRecipientCity(updatedAddressDTO.getRecipientCity());
        existingAddress.setRecipientCountry(updatedAddressDTO.getRecipientCountry());

        orderRepository.save(existOrderId);
        return convertToDTO(existingAddress);
    }

    @Override
    public String initiatePayment(OrderDTO orderDTO) {
        double totalAmount = calculateTotalAmount(orderDTO);
        Map<String, String> paymentResult = paymentServiceClient.createPayPalPayment(totalAmount);
        String approvalLink = paymentResult.get("approvalLink");
        return approvalLink;

    }

    public double calculateTotalAmount(OrderDTO orderDTO) {
        double subTotal = orderDTO.getOrderDetails().stream()
                .mapToDouble(detail->detail.getPrice() * detail.getQuantity())
                .sum();
        return subTotal + orderDTO.getShippingFee();
    }



}