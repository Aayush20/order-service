package org.example.orderservice.services;

import org.example.orderservice.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendOrderConfirmationEmail(String to, String subject, String content) {
        // Simulate email sending
        logger.info("📧 Sending order email to {} with subject '{}': {}", to, subject, content);
    }
}
