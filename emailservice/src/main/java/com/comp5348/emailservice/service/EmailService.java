package com.comp5348.emailservice.service;

import com.comp5348.contracts.EmailRequest;
import com.comp5348.emailservice.model.Email;
import com.comp5348.emailservice.model.EmailStatus;
import com.comp5348.emailservice.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;

    @Transactional
    public void queueEmail(EmailRequest request) {
        Email email = new Email(
                request.getTo(),
                request.getSubject(),
                request.getBody(),
                request.getOrderId()
        );
        emailRepository.save(email);
        log.info("[EmailService] Queued email for orderId={}", request.getOrderId());
    }

    @Scheduled(fixedDelay = 10000) // Run every 10 seconds
    @Transactional
    public void processPendingEmails() {
        List<Email> pendingEmails = emailRepository.findByStatus(EmailStatus.PENDING);
        if (pendingEmails.isEmpty()) {
            return;
        }

        log.info("[EmailService] Processing {} pending emails", pendingEmails.size());

        for (Email email : pendingEmails) {
            try {
                // Simulate sending the email
                log.info("[Email] Sending to='{}' subject='{}' (orderId={})",
                        email.getRecipientAddress(), email.getSubject(), email.getOrderId());

                email.setStatus(EmailStatus.SENT);
                email.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("[EmailService] Failed to send email id={}: {}", email.getId(), e.getMessage());
                email.setStatus(EmailStatus.FAILED);
                email.setErrorMessage(e.getMessage());
            }
            emailRepository.save(email);
        }
    }
}
