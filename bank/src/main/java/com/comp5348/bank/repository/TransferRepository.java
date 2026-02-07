package com.comp5348.bank.repository;

import com.comp5348.bank.model.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {
    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<TransferEntity> findByOrderId(Long orderId);
}
