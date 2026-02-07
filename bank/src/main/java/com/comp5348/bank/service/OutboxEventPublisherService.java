package com.comp5348.bank.service;

import com.comp5348.bank.model.OutboxEventEntity;
import com.comp5348.bank.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Polls the outbox table and publishes events to RabbitMQ.
 * Ensures at-least-once delivery semantics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String BANK_STATUS_EXCHANGE = "bank.status.exchange";
    private static final String BANK_STATUS_ROUTING_KEY = "bank.status";

    /**
     * Runs every 5 seconds to process unprocessed outbox events.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("[Bank Outbox] Processing {} pending events", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            try {
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(
                    BANK_STATUS_EXCHANGE,
                    BANK_STATUS_ROUTING_KEY,
                    event.getPayload()
                );

                // Mark as processed
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("[Bank Outbox] Published event: id={}, type={}, aggregateId={}", 
                        event.getId(), event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                log.error("[Bank Outbox] Failed to publish event id={}: {}", 
                        event.getId(), e.getMessage());
                // Event remains unprocessed and will be retried
            }
        }
    }
}
