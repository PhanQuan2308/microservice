package org.example.orderservice.producer;

import org.example.orderservice.controller.OrderController;
import org.example.orderservice.entity.Order;
import org.example.orderservice.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class OrderProducer {
    @Autowired
private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public void sendOrderEvent(OrderEvent event) {
        try {
            kafkaTemplate.send("order-completed-topic", event).get();
            logger.info("Order event sent successfully: {}", event);
        } catch (Exception e) {
            logger.error("Failed to send order event to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
