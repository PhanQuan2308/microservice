package org.example.productservice.consumer;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    @KafkaListener(topics = "order-topic", groupId = "product_group")
    public void consume(String orderMessage){
        System.out.println("Received order:"+orderMessage);
    }
}
