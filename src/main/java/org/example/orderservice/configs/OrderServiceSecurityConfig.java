package org.example.orderservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("prod")  // Activate this config only in production
public class OrderServiceSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since this is a stateless REST API.
                .csrf(csrf -> csrf.disable())
                // All endpoints require an authenticated user
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // Configure the service as an OAuth2 resource server to automatically
                // extract and validate the JWT from the Authorization header.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}

