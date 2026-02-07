package com.comp5348.store.messaging;

import com.comp5348.contracts.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.comp5348.store.config.RabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(EmailRequest request) {
        log.info("[Store → Email] Sending email request: {}", request);
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, RK_EMAIL_REQUEST, request);
    }
}
