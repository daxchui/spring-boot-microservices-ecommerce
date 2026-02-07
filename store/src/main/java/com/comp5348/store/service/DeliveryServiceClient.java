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
public class DeliveryServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${delivery.service.url}")
    private String deliveryServiceUrl;

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 3000))
    public void requestDelivery(Long orderId, String warehouseLocation, String deliveryAddress) {
        String url = deliveryServiceUrl + "/request";
        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "warehouseLocation", warehouseLocation,
                "deliveryAddress", deliveryAddress
        );

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body), String.class);
            System.out.println("[DeliveryClient] Delivery request sent. Response: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("[DeliveryClient] Error contacting DeliveryCo: " + e.getMessage());
        }
    }


    @Recover
    public boolean recoverDelivery(Exception e, Long orderId, String warehouseLocation, String deliveryAddress) {
        System.err.println("[DeliveryCo] All retries failed for order " + orderId + ": " + e.getMessage());
        return false;
    }
}
