package com.comp5348.deliveryco.messaging;

import com.comp5348.contracts.DeliveryRequest;
import com.comp5348.deliveryco.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.comp5348.deliveryco.config.RabbitMQConfig.DELIVERY_QUEUE;

@RequiredArgsConstructor
@Slf4j
@Component
public class DeliveryConsumer {

    private final DeliveryService deliveryService;

    @RabbitListener(queues = DELIVERY_QUEUE)
    public void handleDelivery(DeliveryRequest request) {
        deliveryService.handleNewRequest(request);
    }
}