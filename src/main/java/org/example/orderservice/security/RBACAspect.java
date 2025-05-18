package org.example.orderservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.example.orderservice.services.TokenService;
import org.example.orderservice.utils.TokenClaimUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RBACAspect {

    private final HttpServletRequest request;
    private final TokenService tokenService;

    public RBACAspect(HttpServletRequest request, TokenService tokenService) {
        this.request = request;
        this.tokenService = tokenService;
    }

    @Before("@annotation(AdminOnly)")
    public void enforceAdminRole(JoinPoint joinPoint) {
        String tokenHeader = request.getHeader("Authorization");
        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasRole(token, "ADMIN")) {
            throw new AccessDeniedException("Only admins allowed");
        }
    }

    @Before("@annotation(hasScope)")
    public void enforceScope(JoinPoint joinPoint, HasScope hasScope) {
        String tokenHeader = request.getHeader("Authorization");
        TokenIntrospectionResponseDTO token = tokenService.introspect(tokenHeader);
        if (!TokenClaimUtils.hasScope(token, hasScope.value())) {
            throw new AccessDeniedException("Missing required scope: " + hasScope.value());
        }
    }
}
