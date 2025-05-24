package org.example.orderservice.integration;


import org.example.orderservice.integration.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisCacheIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldStoreAndRetrieveDataFromRedis() {
        redisTemplate.opsForValue().set("testKey", "testValue");

        String value = redisTemplate.opsForValue().get("testKey");

        assertThat(value).isEqualTo("testValue");
    }
}
