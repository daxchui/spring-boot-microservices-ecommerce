package com.comp5348.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private boolean success;
    private String transactionId;
    private String message;
}
