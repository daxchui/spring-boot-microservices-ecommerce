package com.comp5348.bank.service;

import com.comp5348.bank.dto.RefundRequestDTO;
import com.comp5348.bank.dto.TransferRequestDTO;
import com.comp5348.bank.dto.TransferResponseDTO;
import com.comp5348.bank.model.AccountEntity;
import com.comp5348.bank.model.LedgerEntry;
import com.comp5348.bank.model.OutboxEventEntity;
import com.comp5348.bank.model.TransferEntity;
import com.comp5348.bank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core banking service implementing double-entry accounting with ACID guarantees.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final FaultInjectionService faultInjectionService;

    /**
     * Creates a new transfer with idempotency support.
     *
     * @param request transfer details
     * @param idempotencyKey unique key for idempotent processing
     * @return transfer response
     */
    @Transactional
    public TransferResponseDTO createTransfer(TransferRequestDTO request, String idempotencyKey) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[Bank] createTransfer - correlationId={}, orderId={}, idempotencyKey={}, amount={}",
                correlationId, request.getOrderId(), idempotencyKey, request.getAmount());

        // Check idempotency
        if (idempotencyKey != null) {
            var existing = transferRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                TransferEntity existingTransfer = existing.get();
                log.info("[Bank] Idempotent request detected - returning existing transfer {}", existingTransfer.getId());
                return new TransferResponseDTO(existingTransfer);
            }
        }

        // Simulate failure scenarios
        faultInjectionService.maybeInjectFault(correlationId);

        // Validate accounts
        AccountEntity fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("From account not found: " + request.getFromAccountId()));
        AccountEntity toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("To account not found: " + request.getToAccountId()));

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Check sufficient funds
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            TransferEntity failedTransfer = new TransferEntity();
            failedTransfer.setCorrelationId(correlationId);
            failedTransfer.setIdempotencyKey(idempotencyKey);
            failedTransfer.setFromAccount(fromAccount);
            failedTransfer.setToAccount(toAccount);
            failedTransfer.setAmount(request.getAmount());
            failedTransfer.setStatus(TransferEntity.TransferStatus.FAILED);
            failedTransfer.setFailureReason("Insufficient funds");
            failedTransfer.setOrderId(request.getOrderId());
            failedTransfer.setCompletedAt(LocalDateTime.now());
            transferRepository.save(failedTransfer);

            log.warn("[Bank] Transfer failed - insufficient funds: orderId={}, required={}, available={}",
                    request.getOrderId(), request.getAmount(), fromAccount.getBalance());
            return new TransferResponseDTO(failedTransfer);
        }

        // Create transfer record
        TransferEntity transfer = new TransferEntity();
        transfer.setCorrelationId(correlationId);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setAmount(request.getAmount());
        transfer.setStatus(TransferEntity.TransferStatus.PENDING);
        transfer.setType(TransferEntity.TransferType.CHARGE);
        transfer.setOrderId(request.getOrderId());
        transfer = transferRepository.save(transfer);

        // Perform double-entry accounting
        try {
            // Debit from account
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            accountRepository.save(fromAccount);
            recordLedgerEntry(transfer.getId(), fromAccount.getId(), request.getAmount().negate(), fromAccount.getBalance());

            // Credit to account
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
            accountRepository.save(toAccount);
            recordLedgerEntry(transfer.getId(), toAccount.getId(), request.getAmount(), toAccount.getBalance());

            // Mark success
            transfer.setStatus(TransferEntity.TransferStatus.SUCCEEDED);
            transfer.setCompletedAt(LocalDateTime.now());
            transferRepository.save(transfer);

            // Record outbox event for async notification
            recordOutboxEvent("Transfer", transfer.getId(), "TRANSFER_SUCCEEDED", transfer.getCorrelationId());

            log.info("[Bank] Transfer succeeded - transferId={}, orderId={}, amount={}",
                    transfer.getId(), request.getOrderId(), request.getAmount());

        } catch (Exception e) {
            transfer.setStatus(TransferEntity.TransferStatus.FAILED);
            transfer.setFailureReason(e.getMessage());
            transfer.setCompletedAt(LocalDateTime.now());
            transferRepository.save(transfer);

            log.error("[Bank] Transfer failed - transferId={}, orderId={}, error={}",
                    transfer.getId(), request.getOrderId(), e.getMessage());
            throw e;
        }

        return new TransferResponseDTO(transfer);
    }

    /**
     * Processes a refund by reversing an original transfer.
     */
    @Transactional
    public TransferResponseDTO createRefund(RefundRequestDTO request, String idempotencyKey) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[Bank] createRefund - correlationId={}, originalTransferId={}, orderId={}",
                correlationId, request.getOriginalTransferId(), request.getOrderId());

        // Check idempotency
        if (idempotencyKey != null) {
            var existing = transferRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("[Bank] Idempotent refund request - returning existing");
                return new TransferResponseDTO(existing.get());
            }
        }

        // Find original transfer
        TransferEntity originalTransfer = transferRepository.findById(request.getOriginalTransferId())
                .orElseThrow(() -> new IllegalArgumentException("Original transfer not found"));

        if (originalTransfer.getStatus() != TransferEntity.TransferStatus.SUCCEEDED) {
            throw new IllegalArgumentException("Cannot refund a transfer that did not succeed");
        }

        // Create reverse transfer request
        TransferRequestDTO reverseRequest = new TransferRequestDTO();
        reverseRequest.setFromAccountId(originalTransfer.getToAccount().getId());
        reverseRequest.setToAccountId(originalTransfer.getFromAccount().getId());
        reverseRequest.setAmount(originalTransfer.getAmount());
        reverseRequest.setOrderId(request.getOrderId());

        TransferResponseDTO result = createTransfer(reverseRequest, idempotencyKey);

        // Mark as refund type
        TransferEntity refundTransfer = transferRepository.findById(result.getTransferId()).orElseThrow();
        refundTransfer.setType(TransferEntity.TransferType.REFUND);
        transferRepository.save(refundTransfer);

        log.info("[Bank] Refund completed - refundTransferId={}, originalTransferId={}",
                result.getTransferId(), request.getOriginalTransferId());

        return new TransferResponseDTO(refundTransfer);
    }

    /**
     * Retrieves transfer status by ID.
     */
    @Transactional(readOnly = true)
    public TransferResponseDTO getTransferStatus(Long transferId) {
        TransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        return new TransferResponseDTO(transfer);
    }

    public AccountEntity createAccount(String ownerName) {
        AccountEntity newAccount = new AccountEntity();
        newAccount.setOwnerName(ownerName);
        newAccount.setBalance(new BigDecimal("10000.00"));
        return accountRepository.save(newAccount);
    }

    private void recordLedgerEntry(Long transferId, Long accountId, BigDecimal delta, BigDecimal balanceAfter) {
        LedgerEntry entry = new LedgerEntry();
        entry.setTransferId(transferId);
        entry.setAccountId(accountId);
        entry.setDelta(delta);
        entry.setBalanceAfter(balanceAfter);
        ledgerEntryRepository.save(entry);
        log.debug("[Bank] Ledger entry recorded - accountId={}, delta={}, balance={}",
                accountId, delta, balanceAfter);
    }

    private void recordOutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        outboxEventRepository.save(event);
        log.debug("[Bank] Outbox event recorded - type={}, aggregateId={}", eventType, aggregateId);
    }
}
