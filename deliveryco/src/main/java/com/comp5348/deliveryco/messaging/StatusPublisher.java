package com.comp5348.deliveryco.messaging;

import com.comp5348.contracts.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.comp5348.deliveryco.config.RabbitMQConfig.STATUS_EXCHANGE;
import static com.comp5348.deliveryco.config.RabbitMQConfig.RK_DELIVERY_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(DeliveryStatus status) {
        rabbitTemplate.convertAndSend(STATUS_EXCHANGE, RK_DELIVERY_STATUS, status);
        log.info("[DeliveryCo → Store] Order {} marked {} ({})",
                status.getOrderId(), status.getState(), status.getNote());
    }
}
