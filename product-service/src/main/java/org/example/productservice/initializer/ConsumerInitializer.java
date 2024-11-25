package org.example.productservice.initializer;

import jakarta.annotation.PostConstruct;
import org.example.productservice.consumer.CategoryQueueConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsumerInitializer {

    @Autowired
    private CategoryQueueConsumer categoryQueueConsumer;

    @PostConstruct
    public  void  startConsumers(){
        categoryQueueConsumer.startConsumers();
    }
}
