package org.example.orderservice.service.impl;

import org.example.orderservice.client.PaymentServiceClient;
import org.example.orderservice.client.ProductServiceClient;
import org.example.orderservice.dto.*;
import org.example.orderservice.entity.Address;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderDetail;
import org.example.orderservice.producer.OrderProducer;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.OrderDetailRepository;
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
                logger.info("Product fetched successfully for productId: {}", detail.getProductId());
            } catch (Exception e) {
                logger.error("Failed to fetch product details for productId: {}", detail.getProductId(), e);
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
            logger.info("Converting OrderDetail with productId: {}", detailDTO.getProductId());
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
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        try {
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
                String paymentUrl = paymentServiceClient.createPayPalPayment(totalAmount);
                String paymentToken = extractTokenFromUrl(paymentUrl);

                savedOrder.setPaymentToken(paymentToken);
                orderRepository.save(savedOrder);

                logger.info("Sending email to {}", orderDTO.getAddress().getRecipientEmail());
                emailService.sendEmail(
                        orderDTO.getAddress().getRecipientEmail(),
                        "Order Created Successfully",
                        "Your order has been created successfully. Total amount: " + totalAmount
                );
                logger.info("Email sent successfully to {}", orderDTO.getAddress().getRecipientEmail());


                OrderDTO responseDTO = convertToOrderDTO(savedOrder);
                responseDTO.setPaymentUrl(paymentUrl);
                return responseDTO;




            } catch (Exception e) {
                savedOrder.setStatus("FAILED");
                orderRepository.save(savedOrder);
                emailService.sendEmail(
                        orderDTO.getAddress().getRecipientEmail(),
                        "Order Creation Failed",
                        "An error occurred while creating your order: " + e.getMessage()
                );
                throw new RuntimeException("Failed to initiate payment for Order ID: " + savedOrder.getOrderId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage());
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
    public Long handlePaymentCallback(String token, boolean isPaymentSuccessful) {
        logger.info("Handling payment callback with token: {}, success: {}", token, isPaymentSuccessful);

        Order order = orderRepository.findByPaymentToken(token)
                .orElseThrow(() -> new RuntimeException("Order not found for token: " + token));

        if (isPaymentSuccessful) {
            logger.info("Payment successful. Updating order ID: {} to COMPLETED", order.getOrderId());
            order.setStatus("COMPLETED");
            orderRepository.save(order);

            PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
            paymentRequestDTO.setOrderId(order.getOrderId());
            paymentRequestDTO.setAmount(order.getTotalAmount());
            paymentRequestDTO.setStatus("COMPLETED");
            paymentRequestDTO.setUserId(order.getUserId());
            paymentRequestDTO.setPaymentMethod("PAYPAL");

            try {
                paymentServiceClient.createPayment(paymentRequestDTO);
                logger.info("Payment created successfully for order ID: {}", order.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to create payment for order ID: {}", order.getOrderId(), e);
            }

            for (OrderDetail detail : order.getOrderDetails()) {
                try {
                    productServiceClient.reduceStock(detail.getProductId(), detail.getQuantity());
                    logger.info("Stock reduced for Product ID: {}", detail.getProductId());
                } catch (Exception e) {
                    logger.error("Failed to reduce stock for Product ID: {}", detail.getProductId(), e);
                }
            }

            emailService.sendEmail(
                    order.getAddress().getRecipientEmail(),
                    "Payment Successful",
                    "Your payment for Order ID " + order.getOrderId() + " has been completed successfully."
            );

        } else {
            logger.info("Payment failed. Updating order ID: {} to FAILED", order.getOrderId());
            order.setStatus("FAILED");
            orderRepository.save(order);
            emailService.sendEmail(
                    order.getAddress().getRecipientEmail(),
                    "Payment Failed",
                    "Your payment for Order ID " + order.getOrderId() + " has failed. Please try again."
            );
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
        String paymentUrl = paymentServiceClient.createPayPalPayment(totalAmount);
        return paymentUrl;
    }

    public double calculateTotalAmount(OrderDTO orderDTO) {
        double subTotal = orderDTO.getOrderDetails().stream()
                .mapToDouble(detail->detail.getPrice() * detail.getQuantity())
                .sum();
        return subTotal + orderDTO.getShippingFee();
    }



}
