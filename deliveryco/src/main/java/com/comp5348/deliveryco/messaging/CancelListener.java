package com.comp5348.deliveryco.messaging;

import com.comp5348.deliveryco.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.comp5348.deliveryco.config.RabbitMQConfig.CANCEL_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelListener {

    private final DeliveryService deliveryService;

    @RabbitListener(queues = CANCEL_QUEUE)
    public void handleCancellation(Long orderId) {
        log.info("[DeliveryCo] Received cancellation for order {} — marked as cancelled", orderId);
        deliveryService.cancel(orderId);
    }
}
