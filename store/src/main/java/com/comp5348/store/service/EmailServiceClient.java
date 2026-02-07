package com.comp5348.store.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Retryable(value = { Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void sendEmail(String to, String subject, String body) {
        String url = emailServiceUrl + "/send";
        Map<String, String> request = Map.of(
                "to", to,
                "subject", subject,
                "body", body
        );

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("[EmailService] Email sent to " + to);
        } else {
            throw new RuntimeException("Failed to send email: " + response.getStatusCode());
        }
    }

    @Recover
    public void recoverEmail(Exception e, String to, String subject, String body) {
        System.err.println("[EmailService] All retries failed for " + to + ": " + e.getMessage());
    }
}
