package org.example.orderservice.services;

import org.example.orderservice.clients.AuthClient;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final String CACHE_KEY_PREFIX = "token:introspect:";
    private static final long TTL_SECONDS = 300; // 5 minutes

    @Autowired
    private AuthClient authClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public TokenIntrospectionResponseDTO introspect(String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        String cacheKey = CACHE_KEY_PREFIX + token;

        // 1. Cache check
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof TokenIntrospectionResponseDTO dto) {
            logger.info("✅ Redis cache hit for token introspection");
            return dto;
        }

        // 2. Call auth service
        TokenIntrospectionResponseDTO result = authClient.introspectToken(tokenHeader);

        // 3. Cache it
        redisTemplate.opsForValue().set(cacheKey, result, TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("📦 Stored introspection result in Redis with 5min TTL");

        return result;
    }
}
