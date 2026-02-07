package com.comp5348.deliveryco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shipment")
public class Shipment {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Version
    private Integer version;

    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "current_state")
    private String currentState;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
}

