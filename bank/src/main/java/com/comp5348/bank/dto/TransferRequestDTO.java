package com.comp5348.bank.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String currency = "AUD";
    private String reference;
    private Long orderId;
}
