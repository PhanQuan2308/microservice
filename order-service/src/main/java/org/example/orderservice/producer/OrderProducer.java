package org.example.orderservice.producer;

import org.example.orderservice.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class OrderProducer {

    private static final String TOPIC = "order-topic";
    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);


    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    public void sendOrder(Order order) {
        logger.info("Sending order to Kafka: {}", order);
        kafkaTemplate.send(TOPIC, order);
    }
}
