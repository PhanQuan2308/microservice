package org.example.orderservice.controller;

import jakarta.validation.Valid;
import org.example.orderservice.client.PaymentServiceClient;
import org.example.orderservice.dto.AddressDTO;
import org.example.orderservice.dto.OrderDTO;

import org.example.orderservice.dto.PaymentCallbackDTO;
import org.example.orderservice.response.ApiResponse;
import org.example.orderservice.service.impl.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
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
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        logger.info("Received order payload: {}", orderDTO);

        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logger.info("Extracted userId from token: {}", userId);
            if (principal == null || principal.equals("anonymousUser")) {
                throw new RuntimeException("User is not authenticated");
            }
            orderDTO.setUserId(Long.parseLong(userId));

            OrderDTO createdOrder = orderService.createOrder(orderDTO);

            ApiResponse<OrderDTO> response = ApiResponse.success(
                    createdOrder,
                    "Created order successfully"
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            ApiResponse<OrderDTO> response = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Create order failed",
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            Map<String, String> result = new HashMap<>();
            result.put("orderId", String.valueOf(orderId));
            result.put("status", status);

            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    result,
                    "Order status updated successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error updating order status for orderId {}: {}", orderId, ex.getMessage());
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Failed to update order status",
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/payment-callback")
    public ResponseEntity<ApiResponse<Long>> handlePaymentCallback(@RequestBody PaymentCallbackDTO paymentCallbackDTO) {
        try {
            Long orderId = orderService.handlePaymentCallback(paymentCallbackDTO.getToken(),
                    paymentCallbackDTO.getIsPaymentSuccessful());

            ApiResponse<Long> response;
            if (paymentCallbackDTO.getIsPaymentSuccessful()) {
                response = ApiResponse.success(orderId, "Payment Success");
                return ResponseEntity.ok(response);
            } else {
                response = ApiResponse.error(HttpStatus.BAD_REQUEST, "Payment Failed", orderId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Error handling payment callback: ", e);

            ApiResponse<Long> response = ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to handle payment callback: " + e.getMessage(),
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {
            Page<OrderDTO> orders = orderService.getAllOrders(PageRequest.of(page,size));
            ApiResponse<Page<OrderDTO>> response = ApiResponse.success(
                    orders,
                    "Get all orders successfully"
            );
            return ResponseEntity.ok(response);

        }catch (Exception e){
            logger.error("Error getting all orders: ", e);
            ApiResponse<Page<OrderDTO>> response = ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(),
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            ApiResponse<OrderDTO> response = ApiResponse.success(
                    order,
                    "Get order with id:" +orderId +"successfully"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e){
            logger.error("Error getting order by id: ", e);
            ApiResponse<OrderDTO> response = ApiResponse.error(
                    HttpStatus.NOT_FOUND,
                    "Not found product with id: " + orderId,
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderDTO updatedOrderDTO) {

        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, updatedOrderDTO);
            ApiResponse<OrderDTO> response = ApiResponse.success(
                    updatedOrder,
                    "Update order successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating order: ", e);
            ApiResponse<OrderDTO> response = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "update order failed",
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            ApiResponse<Void> response = ApiResponse.success(
                    null,
                    "Delete order successfully"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e){
            logger.error("Error deleting order: ", e);
            ApiResponse<Void> response = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Delete order fail",
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{orderId}/address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long orderId,
            @RequestBody AddressDTO updatedAddressDTO) {
        try {
            AddressDTO updatedAddress = orderService.updateAddress(orderId, updatedAddressDTO);

            ApiResponse<AddressDTO> response = ApiResponse.success(
                    updatedAddress,
                    "Updated address successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating address: ", e);
            ApiResponse<AddressDTO> response = ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


}