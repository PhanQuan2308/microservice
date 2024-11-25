package org.example.productservice.producer;

import org.example.productservice.entity.Category;
import org.example.productservice.queue.QueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueueProducer {
    @Autowired
    private QueueManager queueManager;

    public void sendToCategoryQueue(Category category){
        queueManager.addToCategoryQueue(category);
        System.out.printf("Added Category:", category.getCategoryName());
    }
}
