package com.comp5348.store.service;

import com.comp5348.store.dto.AccountDTO;
import com.comp5348.store.dto.RegistrationRequestDTO;
import com.comp5348.store.model.Customer;
import com.comp5348.store.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankServiceClient bankServiceClient;

    public Customer registerCustomer(RegistrationRequestDTO request) {
        // Create bank account first
        AccountDTO bankAccount = bankServiceClient.createAccount(request.getFirstName() + " " + request.getLastName());

        Customer customer = new Customer();
        customer.setUsername(request.getUsername());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash password
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setBankAccountId(bankAccount.getId());
        return customerRepository.save(customer);
    }

    public boolean authenticate(String username, String rawPassword) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return passwordEncoder.matches(rawPassword, customer.getPasswordHash());
    }
}
