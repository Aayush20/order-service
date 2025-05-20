package org.example.orderservice.services;

import org.example.orderservice.clients.AuthClient;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    @Mock private AuthClient authClient;
    @Mock private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks private TokenService tokenService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCachedTokenIfAvailable() {
        TokenIntrospectionResponseDTO cached = new TokenIntrospectionResponseDTO();
        cached.setSub("user123");

        when(redisTemplate.opsForValue().get("token:introspect:abc")).thenReturn(cached);

        TokenIntrospectionResponseDTO result = tokenService.introspect("Bearer abc");
        assertThat(result.getSub()).isEqualTo("user123");

        verify(authClient, never()).introspectToken(any());
    }
}
