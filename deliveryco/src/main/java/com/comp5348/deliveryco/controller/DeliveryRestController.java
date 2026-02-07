package com.comp5348.deliveryco.controller;

import com.comp5348.contracts.DeliveryRequest;
import com.comp5348.deliveryco.model.Shipment;
import com.comp5348.deliveryco.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.comp5348.deliveryco.config.RabbitMQConfig.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/delivery")
public class DeliveryRestController {

    private final RabbitTemplate rabbitTemplate;
    private final ShipmentRepository shipmentRepository;

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestDelivery(@RequestBody DeliveryRequest req) {
        Shipment s = shipmentRepository.findById(req.getOrderId()).orElseGet(() -> {
            Shipment ns = new Shipment();
            ns.setOrderId(req.getOrderId());
            return ns;
        });
        s.setWarehouseLocation(req.getWarehouseLocation());
        s.setDeliveryAddress(req.getDeliveryAddress());
        s.setCancelled(false);
        s.setCurrentState("REQUESTED");
        s.setLastUpdateTime(LocalDateTime.now());
        shipmentRepository.save(s);

        rabbitTemplate.convertAndSend(DELIVERY_EXCHANGE, RK_DELIVERY_REQUEST, req);

        return ResponseEntity.accepted().body(Map.of(
                "accepted", true,
                "orderId", req.getOrderId(),
                "message", "Delivery request published to MQ"
        ));
    }


    @GetMapping("/status/{orderId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable("orderId") Long orderId) {
        Optional<Shipment> opt = shipmentRepository.findById(orderId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "found", false,
                    "orderId", orderId,
                    "message", "Shipment not found"
            ));
        }
        Shipment s = opt.get();
        return ResponseEntity.ok(Map.of(
                "found", true,
                "orderId", s.getOrderId(),
                "state", s.getCurrentState(),
                "cancelled", s.isCancelled(),
                "warehouseLocation", s.getWarehouseLocation(),
                "deliveryAddress", s.getDeliveryAddress(),
                "lastUpdateTime", String.valueOf(s.getLastUpdateTime())
        ));
    }
}