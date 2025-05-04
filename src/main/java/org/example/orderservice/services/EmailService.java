package org.example.orderservice.services;

public interface EmailService {
    void sendOrderConfirmationEmail(String to, String subject, String content);
}

