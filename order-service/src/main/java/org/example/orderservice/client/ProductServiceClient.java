package org.example.orderservice.client;

import org.example.orderservice.config.FeignConfig;
import org.example.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {
    @GetMapping("api/v1/products/")
    ProductDTO getProduct(@PathVariable Long id);

    @GetMapping("/api/v1/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @PostMapping("/api/v1/products/reduce-stock")
    void reduceStock(@RequestParam Long productId, @RequestParam Integer quantity);
}
