package org.example.orderservice.services;

import org.example.orderservice.clients.AuthClient;
import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final AuthClient authClient;

    public TokenService(AuthClient authClient) {
        this.authClient = authClient;
    }

    public TokenIntrospectionResponseDTO introspect(String tokenHeader) {
        return authClient.introspectToken(tokenHeader);
    }
}
