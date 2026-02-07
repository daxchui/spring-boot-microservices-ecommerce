package com.comp5348.emailservice.controller;

import com.comp5348.contracts.EmailRequest;
import com.comp5348.emailservice.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.comp5348.emailservice.config.RabbitMQConfig.*;

@RequiredArgsConstructor
@RestController
public class EmailController {

    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;

    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendDirect(@RequestBody SendEmailBody body) {
        EmailRequest req = new EmailRequest(
                body.getTo(),
                body.getSubject(),
                body.getMessage(),
                body.getOrderId()
        );
        emailService.queueEmail(req);
        return ResponseEntity.ok("Email queued");
    }

    @PostMapping("/api/email/send")
    public ResponseEntity<String> sendViaQueue(@RequestBody EmailRequest req) {
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, RK_EMAIL_REQUEST, req);
        return ResponseEntity.accepted().body("queued");
    }

    @Data
    public static class SendEmailBody {
        private String to;
        private String subject;
        private String message;
        private Long orderId;
    }
}
