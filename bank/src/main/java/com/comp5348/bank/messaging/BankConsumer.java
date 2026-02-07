package com.comp5348.bank.messaging;

import com.comp5348.bank.dto.RefundRequestDTO;
import com.comp5348.bank.dto.TransferRequestDTO;
import com.comp5348.bank.dto.TransferResponseDTO;
import com.comp5348.bank.model.TransferEntity;
import com.comp5348.bank.repository.TransferRepository;
import com.comp5348.bank.service.BankingService;
import com.comp5348.contracts.PaymentRequest;
import com.comp5348.contracts.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static com.comp5348.bank.config.RabbitMQConfig.*;

/**
 * Consumes payment requests from RabbitMQ (RPC pattern).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankConsumer {

    private final BankingService bankingService;
    private final TransferRepository transferRepository;

    @RabbitListener(queues = BANK_QUEUE)
    @SendTo(BANK_REPLY_QUEUE)
    public PaymentResponse handlePayment(PaymentRequest request) {
        //  Improved logging format matching spec
        log.info("[Store → Bank] Received {} request: order={} amount=${}", 
                request.getType(), request.getOrderId(), request.getAmount());

        try {
            if (request.getType() == PaymentRequest.Type.CHARGE) {
                PaymentResponse response = handleCharge(request);
            
                if (response.isSuccess()) {
                    log.info("[Bank → Store] Payment approved successfully: order={} txnId={}", 
                            request.getOrderId(), response.getTransactionId());
                } else {
                    log.warn("[Bank → Store] Payment failed: order={} reason={}", 
                            request.getOrderId(), response.getMessage());
                }
            
                return response;
            
            } else if (request.getType() == PaymentRequest.Type.REFUND) {
                PaymentResponse response = handleRefund(request);
            
                if (response.isSuccess()) {
                    log.info("[Bank → Store] Refund processed successfully: order={} txnId={}", 
                            request.getOrderId(), response.getTransactionId());
                } else {
                    log.warn("[Bank → Store] Refund failed: order={} reason={}", 
                            request.getOrderId(), response.getMessage());
                }
            
                return response;
            
            } else {
                return new PaymentResponse(
                        request.getOrderId(), 
                        false, 
                        null, 
                        "Unknown payment type: " + request.getType()
                );
            }
        } catch (Exception e) {
            log.error("[Bank → Store] Payment processing failed: order={} error={}", 
                    request.getOrderId(), e.getMessage(), e);
            return new PaymentResponse(
                    request.getOrderId(), 
                    false, 
                    null, 
                    "Payment failed: " + e.getMessage()
            );
        }
    }

    private PaymentResponse handleCharge(PaymentRequest request) {
        TransferRequestDTO transferRequest = new TransferRequestDTO();
        transferRequest.setFromAccountId(Long.parseLong(request.getCustomerAccountId()));
        transferRequest.setToAccountId(request.getStoreAccountId());
        transferRequest.setAmount(BigDecimal.valueOf(request.getAmount()));
        transferRequest.setOrderId(request.getOrderId());

        String idempotencyKey = "charge-" + request.getOrderId() + "-" + UUID.randomUUID();

        TransferResponseDTO transferResult = bankingService.createTransfer(transferRequest, idempotencyKey);

    boolean success = "SUCCEEDED".equals(transferResult.getStatus());
    
    // Generate spec-compliant transaction ID
    String transactionId = success 
            ? "TXN-" + System.currentTimeMillis() + "-" + transferResult.getTransferId()
            : null;
    
    String message = success 
            ? "Payment approved successfully" 
            : "Payment failed: " + transferResult.getFailureReason();

    log.info("[Bank] Charge {} - orderId={}, txnId={}", 
            success ? "succeeded" : "failed", 
            request.getOrderId(), 
            transactionId);

    return new PaymentResponse(
            request.getOrderId(), 
            success, 
            transactionId,  // Now returns "TXN-1730182333123-456"
            message
    );
}

private PaymentResponse handleRefund(PaymentRequest request) {
    TransferEntity originalTransfer = transferRepository.findByOrderId(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Original transfer not found for order " + request.getOrderId()));

    RefundRequestDTO refundRequest = new RefundRequestDTO();
    refundRequest.setOriginalTransferId(originalTransfer.getId());
    refundRequest.setOrderId(request.getOrderId());

    String idempotencyKey = "refund-" + request.getOrderId() + "-" + UUID.randomUUID();

    TransferResponseDTO transferResult = bankingService.createRefund(refundRequest, idempotencyKey);

    boolean success = "SUCCEEDED".equals(transferResult.getStatus());
    
    // Generate spec-compliant transaction ID
    String transactionId = success 
            ? "TXN-" + System.currentTimeMillis() + "-" + transferResult.getTransferId()
            : null;
    
    String message = success 
            ? "Refund processed successfully" 
            : "Refund failed: " + transferResult.getFailureReason();

    log.info("[Bank] Refund {} - orderId={}, txnId={}", 
            success ? "succeeded" : "failed", 
            request.getOrderId(), 
            transactionId);

    return new PaymentResponse(
            request.getOrderId(), 
            success, 
            transactionId,  // Now returns "TXN-1730182333123-789"
            message
    );
}}
