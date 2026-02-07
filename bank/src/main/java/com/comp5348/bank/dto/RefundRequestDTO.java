package com.comp5348.bank.dto;

import lombok.Data;

@Data
public class RefundRequestDTO {
    private Long originalTransferId;
    private String reason;
    private Long orderId;
}
