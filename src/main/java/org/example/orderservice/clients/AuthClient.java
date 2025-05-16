package org.example.orderservice.clients;

import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service", url = "http://auth-service") // or use service discovery
public interface AuthClient {

    @PostMapping(value = "/auth/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    TokenIntrospectionResponseDTO introspectToken(@RequestHeader("Authorization") String bearerToken);
}
