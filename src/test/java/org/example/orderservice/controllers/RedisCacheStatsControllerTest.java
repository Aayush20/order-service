package org.example.orderservice.controllers;

import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.example.orderservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedisCacheStatsController.class)
@Import(RedisCacheStatsControllerTest.TestConfig.class)
public class RedisCacheStatsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TokenService tokenService;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private CacheManager cacheManager;

    private final String token = "Bearer admin-token";

    @TestConfiguration
    static class TestConfig {
        @Bean public RedisTemplate<String, Object> redisTemplate() { return mock(RedisTemplate.class); }
        @Bean public CacheManager cacheManager() { return mock(CacheManager.class); }
        @Bean public TokenService tokenService() { return mock(TokenService.class); }
    }

    @BeforeEach
    void setup() {
        TokenIntrospectionResponseDTO tokenDto = new TokenIntrospectionResponseDTO();
        tokenDto.setSub("admin");
        tokenDto.setRoles(List.of("ADMIN"));
        when(tokenService.introspect(token)).thenReturn(tokenDto);

        when(redisTemplate.keys("*")).thenReturn(Set.of("key1", "key2"));
        when(cacheManager.getCacheNames()).thenReturn(Set.of("orders", "products"));
    }

    @Test
    void shouldReturnRedisStatsForAdmin() throws Exception {
        mockMvc.perform(get("/admin/cache-stats").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyCount").value(2))
                .andExpect(jsonPath("$.cacheNames").isArray())
                .andExpect(jsonPath("$.keys").isArray());
    }
}
