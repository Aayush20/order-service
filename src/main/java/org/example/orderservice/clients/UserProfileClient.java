package org.example.orderservice.clients;

import org.example.orderservice.dtos.UserProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProfileClient {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public UserProfileClient(RestTemplate restTemplate,
                             @Value("${auth.service.url}") String authServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl;
    }

    public UserProfileDTO getUserProfile(String bearerToken) {
        String url = authServiceBaseUrl + "/users/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, UserProfileDTO.class
            );
            return response.getBody();
        } catch (Exception ex) {
            logger.error("❌ Failed to fetch user profile from auth-service: {}", ex.getMessage(), ex);
            throw new RuntimeException("Could not fetch user profile from auth-service");
        }
    }
}

