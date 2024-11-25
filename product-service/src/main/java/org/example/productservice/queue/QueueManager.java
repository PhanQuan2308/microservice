package org.example.productservice.queue;

import org.example.productservice.entity.Category;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class QueueManager {
    private final BlockingQueue<Category> categoryQueue = new LinkedBlockingQueue<>();

    public void addToCategoryQueue(Category category) {
        categoryQueue.add(category);
    }

    public BlockingQueue<Category> getCategoryQueue() {
        return categoryQueue;
    }
}
