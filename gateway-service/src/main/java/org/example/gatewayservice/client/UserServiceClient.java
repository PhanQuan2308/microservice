package org.example.gatewayservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "http://localhost:8080/api/v1/user")
public interface UserServiceClient {
    @GetMapping("/token/blacklist/check")
    Boolean isTokenBlacklisted(@RequestParam("token") String token);
}
