package org.example.orderservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OrderServiceSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/order/orders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/order/placeorder").authenticated()
                        .requestMatchers(HttpMethod.POST, "/order/cart/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/order/cart").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/order/orders/**/cancel").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/order/orders/**/status").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/order/orders/**/audit-log").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
