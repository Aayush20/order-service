package org.example.orderservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.example.orderservice.services.TokenService;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(SecurityIntegrationTest.Config.class)
public class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TokenService tokenService;

    @TestConfiguration
    static class Config {
        @Bean public TokenService tokenService() {
            return mock(TokenService.class);
        }
    }

    @Test
    void shouldReturn401ForNoToken() throws Exception {
        mockMvc.perform(get("/order/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403ForAdminOnlyEndpointWithUserToken() throws Exception {
        TokenIntrospectionResponseDTO userToken = new TokenIntrospectionResponseDTO();
        userToken.setSub("user123");
        userToken.setRoles(List.of("CUSTOMER"));
        when(tokenService.introspect("Bearer user-token")).thenReturn(userToken);

        mockMvc.perform(get("/admin/cache-stats").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403ForMissingScope() throws Exception {
        TokenIntrospectionResponseDTO userToken = new TokenIntrospectionResponseDTO();
        userToken.setSub("user123");
        userToken.setScopes(List.of()); // No `internal`
        when(tokenService.introspect("Bearer no-scope")).thenReturn(userToken);

        mockMvc.perform(post("/order/internal/rollback-retry")
                        .header("Authorization", "Bearer no-scope"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenTokenIsExpired() throws Exception {
        TokenIntrospectionResponseDTO expiredToken = new TokenIntrospectionResponseDTO();
        expiredToken.setActive(false); // simulate expired JWT

        when(tokenService.introspect("Bearer expired")).thenReturn(expiredToken);

        mockMvc.perform(get("/order/cart")
                        .header("Authorization", "Bearer expired"))
                .andExpect(status().isUnauthorized());
    }

}
