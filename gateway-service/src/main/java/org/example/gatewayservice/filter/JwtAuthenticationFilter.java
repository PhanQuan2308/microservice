package org.example.gatewayservice.filter;
import org.example.gatewayservice.client.UserServiceClient;
import org.example.gatewayservice.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        logger.debug("Incoming Authorization Header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            logger.debug("Extracted JWT Token: {}", jwt);

            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/api/v1/user/token/blacklist/check?token=" + jwt)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .flatMap(isBlacklisted -> {
                        logger.debug("Blacklist Check Result for Token: {} is {}", jwt, isBlacklisted);

                        if (!isBlacklisted && jwtUtil.validateJwtToken(jwt)) {
                            String userId = jwtUtil.getUserIdFromJwtToken(jwt);
                            System.out.println("Extracted userId from JWT: " + userId);
                            String username = jwtUtil.getUsernameFromJwtToken(jwt);
                            String role = jwtUtil.getRoleFromJwtToken(jwt);
                            System.out.println("Adding headers in JwtAuthenticationFilter - userId: " + userId + ", role: " + role);
                            logger.debug("Token validated successfully. Extracted userId: {}, username: {}, role: {}", userId, username, role);

                            ServerWebExchange modifiedExchange = exchange.mutate()
                                    .request(r -> r.headers(headers -> {
                                        headers.add("X-User-Id", userId);
                                        headers.add("X-User-Role", role);
                                        logger.debug("Adding X-User-Id: {} and X-User-Role: {}", userId, role);
                                    }))
                                    .build();

                            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    new User(username, "", authorities),
                                    null,
                                    authorities
                            );

                            return chain.filter(modifiedExchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        } else {
                            System.out.println("Token validation failed or token is blacklisted");
                            return chain.filter(exchange);
                        }
                    });
        }

        System.out.println("JWT token not valid or Authorization header missing");
        return chain.filter(exchange);
    }
}
