package com.comp5348.bank.controller;

import com.comp5348.bank.dto.AccountCreationRequestDTO;
import com.comp5348.bank.model.AccountEntity;
import com.comp5348.bank.service.BankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final BankingService bankingService;

    @PostMapping
    public ResponseEntity<AccountEntity> createAccount(@RequestBody AccountCreationRequestDTO request) {
        AccountEntity newAccount = bankingService.createAccount(request.getOwnerName());
        return ResponseEntity.ok(newAccount);
    }
}
