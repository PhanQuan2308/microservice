package org.example.productservice.consumer;


import org.example.productservice.event.OrderEvent;
import org.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    @Autowired
    private ProductService productService;

    @KafkaListener(topics = "order-stock-reduction-topic", groupId = "product-service-group")
    public void handleOrderEvent(OrderEvent event) {
        try {
            productService.reduceStock(event.getStockReductions());
            System.out.println("Stock reduced for order: " + event.getOrderId());
        } catch (Exception e) {
            System.err.println("Failed to reduce stock for order: " + event.getOrderId());
            e.printStackTrace();
        }
    }
}
