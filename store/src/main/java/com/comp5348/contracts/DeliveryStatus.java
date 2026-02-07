package com.comp5348.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryStatus {

    public enum State {
        REQUESTED,
        IN_TRANSIT,
        DELIVERED,
        LOST
    }

    private Long orderId;
    private State state;
    private String note;
}
