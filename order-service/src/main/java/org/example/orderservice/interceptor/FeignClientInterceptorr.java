package org.example.orderservice.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class FeignClientInterceptorr implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Lấy userId từ principal
            String userId = authentication.getPrincipal().toString();

            // Lấy role từ authorities
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);

            // Thêm các header vào request Feign
            template.header("X-User-Id", userId);
            template.header("X-User-Role", role);
        }
    }
}
