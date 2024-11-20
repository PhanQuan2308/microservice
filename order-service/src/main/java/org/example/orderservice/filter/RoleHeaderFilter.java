package org.example.orderservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class RoleHeaderFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RoleHeaderFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");

        if (role == null || userId == null) {
            logger.warn("Missing X-User-Role or X-User-Id headers. Skipping authentication.");
        } else {
            try {
                // Validate userId is a valid long
                Long.parseLong(userId);

                // Create authentication token
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singleton(new SimpleGrantedAuthority(role))
                );

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.info("Successfully set authentication for userId: {} with role: {}", userId, role);
            } catch (NumberFormatException ex) {
                logger.error("Invalid userId format in X-User-Id header: {}", userId, ex);
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
