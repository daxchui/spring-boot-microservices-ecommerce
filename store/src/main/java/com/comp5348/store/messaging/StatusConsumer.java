package com.comp5348.store.messaging;

import com.comp5348.contracts.DeliveryStatus;
import com.comp5348.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.comp5348.store.config.RabbitMQConfig.STATUS_DELIVERY_QUEUE;

@Component
@RequiredArgsConstructor
public class StatusConsumer {

    private final OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(StatusConsumer.class);

    @RabbitListener(queues = STATUS_DELIVERY_QUEUE)
    public void onDeliveryStatusReceived(DeliveryStatus status) {
        log.info("[Store ← DeliveryCo] Received delivery status: orderId={} state={} note={}",
                status.getOrderId(), status.getState(), status.getNote());

        try {
            String storeStatus = (status.getState() == DeliveryStatus.State.LOST) 
                ? "DELIVERY_LOST" 
                : status.getState().name();

            boolean updated = orderService.updateOrderStatus(status.getOrderId(), storeStatus);
            if (updated) {
                log.info("[Store] Order {} updated in database to {}", status.getOrderId(), storeStatus);
            } else {
                log.warn("[Store] Ignored status update for order {} — already finalized", status.getOrderId());
            }
        } catch (Exception e) {
            log.error("[Store] Failed to update order {}: {}", status.getOrderId(), e.getMessage());
        }
    }
}
