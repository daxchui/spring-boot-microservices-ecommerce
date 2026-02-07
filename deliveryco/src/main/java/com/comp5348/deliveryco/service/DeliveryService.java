package com.comp5348.deliveryco.service;

import com.comp5348.contracts.DeliveryRequest;
import com.comp5348.contracts.DeliveryStatus;
import com.comp5348.deliveryco.model.Shipment;
import com.comp5348.deliveryco.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.comp5348.contracts.DeliveryStatus.State.*;
import static com.comp5348.deliveryco.config.RabbitMQConfig.RK_DELIVERY_STATUS;
import static com.comp5348.deliveryco.config.RabbitMQConfig.STATUS_EXCHANGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final RabbitTemplate rabbitTemplate;
    private final ShipmentRepository shipmentRepository;
    private final Set<Long> cancelledOrders = ConcurrentHashMap.newKeySet();

    @Transactional
    public void handleNewRequest(DeliveryRequest request) {
        Long orderId = request.getOrderId();

        if (isCancelled(orderId)) {
            log.warn("[DeliveryCo] Ignoring delivery request for already-cancelled order {}", orderId);
            return;
        }

        log.info("[DeliveryCo] Received delivery request: orderId={} from='{}' to='{}'",
                orderId, request.getWarehouseLocation(), request.getDeliveryAddress());

        // Create or update the shipment record
        Shipment shipment = shipmentRepository.findById(orderId).orElseGet(() -> {
            Shipment newShipment = new Shipment();
            newShipment.setOrderId(orderId);
            return newShipment;
        });

        shipment.setWarehouseLocation(request.getWarehouseLocation());
        shipment.setDeliveryAddress(request.getDeliveryAddress());
        shipment.setCancelled(false);
        shipment.setCurrentState("REQUESTED");
        shipment.setLastUpdateTime(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Trigger the async delivery processing
        processDelivery(orderId);
    }

    @Async
    @Transactional
    public void processDelivery(Long orderId) {
        try {
            // Simulate processing time before transit
            Thread.sleep(20000);

            if (isCancelled(orderId)) {
                log.warn("[DeliveryCo] Delivery process for order {} aborted; already cancelled.", orderId);
                return;
            }

            // Mark as IN_TRANSIT
            Shipment shipment = shipmentRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Shipment not found"));

            shipment.setCurrentState("IN_TRANSIT");
            shipment.setLastUpdateTime(LocalDateTime.now());
            shipmentRepository.save(shipment);

            rabbitTemplate.convertAndSend(STATUS_EXCHANGE, RK_DELIVERY_STATUS,
                    new DeliveryStatus(orderId, IN_TRANSIT, "Package picked up by courier"));
            log.info("[DeliveryCo → Store] Order {} marked IN_TRANSIT", orderId);

            // Simulate delivery time
            Thread.sleep(5000);

            // Check for cancellation during transit
            if (isCancelled(orderId)) {
                log.warn("[DeliveryCo] Final status update skipped for order {} (cancelled by Store)", orderId);
                return;
            }

            // Determine final state (LOST or DELIVERED)
            boolean lost = Math.random() < 0.05;
            DeliveryStatus.State finalState = lost ? LOST : DELIVERED;
            String note = lost ? "Package lost in transit" : "Package delivered successfully";

            shipment.setCurrentState(lost ? "LOST" : "DELIVERED");
            shipment.setLastUpdateTime(LocalDateTime.now());
            shipmentRepository.save(shipment);

            rabbitTemplate.convertAndSend(STATUS_EXCHANGE, RK_DELIVERY_STATUS,
                    new DeliveryStatus(orderId, finalState, note));
            log.info("[DeliveryCo → Store] Order {} marked {}", orderId, finalState);

        } catch (InterruptedException e) {
            log.error("Delivery process for order {} was interrupted", orderId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error processing delivery for order {}", orderId, e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<String> getState(Long orderId) {
        return shipmentRepository.findById(orderId).map(Shipment::getCurrentState);
    }

    @Transactional
    public void cancel(Long orderId) {
        cancelledOrders.add(orderId);
        Shipment shipment = shipmentRepository.findById(orderId).orElseGet(() -> {
            Shipment newShipment = new Shipment();
            newShipment.setOrderId(orderId);
            return newShipment;
        });

        shipment.setCancelled(true);
        shipment.setCurrentState("CANCELLED");
        shipment.setLastUpdateTime(LocalDateTime.now());
        shipmentRepository.save(shipment);
    }

    public boolean isCancelled(Long orderId) {
        return cancelledOrders.contains(orderId);
    }
}