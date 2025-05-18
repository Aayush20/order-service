package org.example.orderservice.controllers;

import org.example.orderservice.security.AdminOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/cache-stats")
public class RedisCacheStatsController {

    @Autowired private CacheManager cacheManager;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    @AdminOnly
    public Map<String, Object> getCacheStats() {
        Set<String> keys = redisTemplate.keys("*");
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheNames", cacheManager.getCacheNames());
        stats.put("keyCount", keys != null ? keys.size() : 0);
        stats.put("keys", keys);
        return stats;
    }
}
