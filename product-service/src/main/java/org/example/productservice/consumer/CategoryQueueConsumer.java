package org.example.productservice.consumer;

import org.example.productservice.entity.Category;
import org.example.productservice.queue.QueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CategoryQueueConsumer {
    @Autowired
    private QueueManager queueManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public void startConsumers(){
        executor.submit(()->{
            while(true){
                try {
                    Category category = queueManager.getCategoryQueue().take();
                    cacheCategory(category);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void cacheCategory(Category category){
        String key = "category_" + category.getCategoryId();
        redisTemplate.opsForValue().set(key, category);
        System.out.printf("Cached category:" + category.getCategoryName());
    }
}
