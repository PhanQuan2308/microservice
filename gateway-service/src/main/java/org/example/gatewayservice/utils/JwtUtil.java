package org.example.gatewayservice.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final SecretKey key;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    public boolean validateJwtToken(String token) {
        logger.debug("Validating JWT: {}", token);
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.debug("JWT Validation Success for Token: {}", token);
            return true;
        } catch (JwtException e) {
            logger.error("JWT Validation Failed for Token: {}. Reason: {}", token, e.getMessage());
            return false;
        }
    }


    public String getUsernameFromJwtToken(String token) {
        logger.debug("Extracting Username from Token: {}", token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String getRoleFromJwtToken(String token) {
        logger.debug("Extracting Role from Token: {}", token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public String getUserIdFromJwtToken(String token) {
        logger.debug("Extracting UserId from Token: {}", token);
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId", String.class);
    }
}
