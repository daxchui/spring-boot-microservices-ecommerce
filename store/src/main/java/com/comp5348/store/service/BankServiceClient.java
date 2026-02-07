package com.comp5348.store.service;

import com.comp5348.store.dto.AccountCreationRequestDTO;
import com.comp5348.store.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BankServiceClient {

    private final RestTemplate restTemplate;

    @Value("${bank.service.url}")
    private String bankServiceUrl;

    public AccountDTO createAccount(String ownerName) {
        AccountCreationRequestDTO request = new AccountCreationRequestDTO(ownerName);
        String url = bankServiceUrl.replace("/api/bank", "/api/accounts");
        return restTemplate.postForObject(url, request, AccountDTO.class);
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public boolean transferPayment(Long customerId, double amount) {
        String url = bankServiceUrl + "/transfer";
        Map<String, Object> request = Map.of(
                "fromCustomerId", customerId,
                "toStoreId", 1L,
                "amount", amount
        );

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    @Recover
    public boolean recoverFromFailure(Exception e, Long customerId, double amount) {
        System.err.println("[BankService] All retries failed for customer " + customerId + ": " + e.getMessage());
        return false; // gracefully fallback
    }

    @Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public boolean refundPayment(Long customerId, double amount) {
        String url = bankServiceUrl + "/refund";
        Map<String, Object> request = Map.of(
                "toCustomerId", customerId,
                "fromStoreId", 1L,
                "amount", amount
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(request), String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("[BankService] Refund failed: " + e.getMessage());
            return false;
        }
    }

}
