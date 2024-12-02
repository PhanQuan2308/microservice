package org.example.productservice.component;

import org.example.productservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisUpdater {

    @Autowired
    private CategoryService categoryService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCategoryInRedis() {
        System.out.printf("Starting Redis update at 2 AM ...");
        categoryService.updateAllCategoriesToRedis();
        System.out.printf("Redis update completed!");
    }
}
