package com.comp5348.store.messaging;

import com.comp5348.contracts.PaymentRequest;
import com.comp5348.contracts.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.comp5348.store.config.RabbitMQConfig.BANK_EXCHANGE;
import static com.comp5348.store.config.RabbitMQConfig.RK_BANK_REQUEST;

@Component
@RequiredArgsConstructor
public class BankMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final long MESSAGE_TTL = 10000; // 10 seconds

    public PaymentResponse sendPayment(PaymentRequest request) {
        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(MESSAGE_TTL));
            return message;
        };

        return (PaymentResponse) rabbitTemplate.convertSendAndReceive(
                BANK_EXCHANGE, RK_BANK_REQUEST, request, messagePostProcessor
        );
    }
}
