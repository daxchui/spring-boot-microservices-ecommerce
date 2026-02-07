package com.comp5348.emailservice.messaging;

import com.comp5348.contracts.EmailRequest;
import com.comp5348.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.comp5348.emailservice.config.RabbitMQConfig.EMAIL_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = EMAIL_QUEUE)
    public void handleEmail(EmailRequest request) {
        log.info("[Email] Received request for orderId={}", request.getOrderId());
        emailService.queueEmail(request);
    }
}
