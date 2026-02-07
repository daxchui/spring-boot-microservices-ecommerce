
package com.comp5348.bank.dto;

import com.comp5348.bank.model.TransferEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponseDTO {
    private Long transferId;
    private String correlationId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String status;
    private String type;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long orderId;

    public TransferResponseDTO(TransferEntity transfer) {
        this.transferId = transfer.getId();
        this.correlationId = transfer.getCorrelationId();
        this.fromAccountId = transfer.getFromAccount() != null ? transfer.getFromAccount().getId() : null;
        this.toAccountId = transfer.getToAccount() != null ? transfer.getToAccount().getId() : null;
        this.amount = transfer.getAmount();
        this.status = transfer.getStatus().name();
        this.type = transfer.getType().name();
        this.failureReason = transfer.getFailureReason();
        this.createdAt = transfer.getCreatedAt();
        this.completedAt = transfer.getCompletedAt();
        this.orderId = transfer.getOrderId();
    }
}
