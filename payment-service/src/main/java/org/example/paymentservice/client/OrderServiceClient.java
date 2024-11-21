package org.example.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    @PutMapping("/api/v1/orders/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);
}
