package org.example.orderservice.config;

import org.example.orderservice.interceptor.FeignClientInterceptorr;
import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Bean
    public FeignClientInterceptorr feignClientInterceptorr() {
        return new FeignClientInterceptorr();
    }
}
