package com.comp5348.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    public enum Type {
        CHARGE, REFUND
    }

    private Long orderId;
    private Long storeAccountId;
    private String customerAccountId;
    private double amount;
    private Type type;
}
