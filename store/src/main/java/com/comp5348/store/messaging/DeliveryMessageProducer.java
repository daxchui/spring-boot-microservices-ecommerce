package com.comp5348.store.messaging;

import com.comp5348.contracts.DeliveryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.comp5348.store.config.RabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendDeliveryRequest(DeliveryRequest request) {
        log.info("[Store → DeliveryCo] Sending delivery request: {}", request);
        rabbitTemplate.convertAndSend(DELIVERY_EXCHANGE, RK_DELIVERY_REQUEST, request);
    }
}
