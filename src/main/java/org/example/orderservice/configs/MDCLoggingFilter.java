package org.example.orderservice.configs;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            String requestId = UUID.randomUUID().toString();
            String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";

            MDC.put("requestId", requestId);
            MDC.put("userId", userId);

            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}

