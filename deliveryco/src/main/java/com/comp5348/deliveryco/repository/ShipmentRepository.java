package com.comp5348.deliveryco.repository;

import com.comp5348.deliveryco.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
