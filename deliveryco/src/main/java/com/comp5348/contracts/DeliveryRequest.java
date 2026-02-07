package com.comp5348.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequest {
    private Long orderId;
    private String warehouseLocation;
    private String deliveryAddress;
}
