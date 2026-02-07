package com.comp5348.store.controller;

import com.comp5348.store.dto.RegistrationRequestDTO;
import com.comp5348.store.model.Customer;
import com.comp5348.store.repository.CustomerRepository;
import com.comp5348.store.security.JwtUtil;
import com.comp5348.store.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegistrationRequestDTO request) {
        userService.registerCustomer(request);
        return "User registered successfully!";
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Customer user = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(username);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("customerId", user.getId());
        response.put("username", user.getUsername());
        return response;

    }
}
