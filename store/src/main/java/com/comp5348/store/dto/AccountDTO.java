package com.comp5348.store.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long id;
    private String ownerName;
    private BigDecimal balance;
}
