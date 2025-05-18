package org.example.orderservice.utils;

import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;

import java.util.List;

public class TokenClaimUtils {

    public static boolean hasRole(TokenIntrospectionResponseDTO token, String role) {
        return token.getRoles() != null && token.getRoles().contains(role);
    }

    public static boolean hasScope(TokenIntrospectionResponseDTO token, String scope) {
        return token.getScopes() != null && token.getScopes().contains(scope);
    }

    public static boolean isSystemCall(TokenIntrospectionResponseDTO token, String expectedSub) {
        return token.getSub() != null && token.getSub().equalsIgnoreCase(expectedSub);
    }

    public static boolean hasAnyScope(TokenIntrospectionResponseDTO token, List<String> requiredScopes) {
        return token.getScopes() != null && requiredScopes.stream().anyMatch(token.getScopes()::contains);
    }

    public static String getUserId(TokenIntrospectionResponseDTO token) {
        return token.getSub();
    }
}
