package org.example.orderservice.service;

import org.example.orderservice.client.PaymentServiceClient;
import org.example.orderservice.client.ProductServiceClient;
import org.example.orderservice.dto.*;
import org.example.orderservice.entity.Address;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderDetail;
import org.example.orderservice.producer.OrderProducer;
import org.example.orderservice.repository.OrderRepository;
import org.example.orderservice.repository.OrderDetailRepository;
import org.example.orderservice.service.impl.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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


        OrderDTO createdOrderDTO = convertToOrderDTO(savedOrder);
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

    @Override
    public OrderDTO createOrderAfterPayment(OrderDTO orderDTO, String token) {
        try {
            boolean isPaymentVerified = paymentServiceClient.verifyPayment(token, calculateTotalAmount(orderDTO));


            if (isPaymentVerified) {
                logger.info("Payment verified successfully, creating order...");

                Order order = convertToOrderEntity(orderDTO);
                order.setStatus("COMPLETED");
                order.setTotalAmount(calculateTotalAmount(orderDTO));

                Order savedOrder = orderRepository.save(order);
                orderDetailRepository.saveAll(order.getOrderDetails());

                OrderDTO result = convertToOrderDTO(savedOrder);

                PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
                paymentRequestDTO.setOrderId(savedOrder.getOrderId());
                paymentRequestDTO.setAmount(result.getTotalAmount());
                paymentRequestDTO.setStatus("COMPLETED");
                paymentRequestDTO.setUserId(orderDTO.getUserId());
                paymentRequestDTO.setPaymentMethod("PAYPAL");
                paymentServiceClient.createPayment(paymentRequestDTO);

                return result;
            } else {
                logger.error("Payment not verified for token: " + token);
                throw new RuntimeException("Payment not verified");
            }
        } catch (Exception e) {
            logger.error("An error occurred in createOrderAfterPayment: " + e.getMessage(), e);
            throw e;
        }
    }

}
