package com.comp5348.deliveryco.controller;

import com.comp5348.contracts.DeliveryRequest;
import com.comp5348.deliveryco.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DeliveryApiController {

    private final DeliveryService deliveryService;

    @PostMapping("/requestDelivery")
    public ResponseEntity<Void> requestDelivery(@RequestBody DeliveryRequest req) {
        deliveryService.handleNewRequest(req);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getStatus(@PathVariable Long orderId) {
        return deliveryService.getState(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

