package org.example.orderservice.controllers;

import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.example.orderservice.services.InventoryRollbackService;
import org.example.orderservice.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RollbackRetryController.class)
@Import(RollbackRetryControllerTest.TestConfig.class)
class RollbackRetryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private InventoryRollbackService rollbackService;
    @Autowired private TokenService tokenService;

    private final String token = "Bearer internal-token";

    @TestConfiguration
    static class TestConfig {
        @Bean public InventoryRollbackService rollbackService() { return mock(InventoryRollbackService.class); }
        @Bean public TokenService tokenService() { return mock(TokenService.class); }
    }

    @Test
    void shouldAllowInternalScopeToTriggerRollbackRetry() throws Exception {
        TokenIntrospectionResponseDTO tokenDto = new TokenIntrospectionResponseDTO();
        tokenDto.setSub("internal-service");
        tokenDto.setScopes(List.of("internal"));

        when(tokenService.introspect(token)).thenReturn(tokenDto);
        doNothing().when(rollbackService).retryFailedTasks();

        mockMvc.perform(post("/order/internal/rollback-retry").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Retry triggered"));
    }
}
