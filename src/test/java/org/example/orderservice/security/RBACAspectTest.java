package org.example.orderservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.example.orderservice.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RBACAspectTest {

    private HttpServletRequest mockRequest;
    private TokenService tokenService;
    private RBACAspect rbacAspect;

    @BeforeEach
    void setup() {
        mockRequest = mock(HttpServletRequest.class);
        tokenService = mock(TokenService.class);
        rbacAspect = new RBACAspect(mockRequest, tokenService);
    }

    @Test
    void shouldAllowAdminRole_whenTokenHasAdmin() {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer token");

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setRoles(List.of("ADMIN"));

        when(tokenService.introspect("Bearer token")).thenReturn(token);

        assertDoesNotThrow(() -> rbacAspect.enforceAdminRole(mock(JoinPoint.class)));
    }

    @Test
    void shouldThrowAccessDenied_whenTokenLacksAdmin() {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer token");

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setRoles(List.of("CUSTOMER"));

        when(tokenService.introspect("Bearer token")).thenReturn(token);

        assertThrows(AccessDeniedException.class, () -> rbacAspect.enforceAdminRole(mock(JoinPoint.class)));
    }

    @Test
    void shouldAllowScope_whenTokenHasScope() {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer token");

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setScopes(List.of("internal", "write"));

        when(tokenService.introspect("Bearer token")).thenReturn(token);

        HasScope mockAnnotation = mock(HasScope.class);
        when(mockAnnotation.value()).thenReturn("internal");

        assertDoesNotThrow(() -> rbacAspect.enforceScope(mock(JoinPoint.class), mockAnnotation));
    }

    @Test
    void shouldThrowAccessDenied_whenTokenMissingScope() {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer token");

        TokenIntrospectionResponseDTO token = new TokenIntrospectionResponseDTO();
        token.setScopes(List.of("external"));

        when(tokenService.introspect("Bearer token")).thenReturn(token);

        HasScope mockAnnotation = mock(HasScope.class);
        when(mockAnnotation.value()).thenReturn("internal");

        assertThrows(AccessDeniedException.class, () -> rbacAspect.enforceScope(mock(JoinPoint.class), mockAnnotation));
    }

}
