package com.comp5348.bank.repository;

import com.comp5348.bank.model.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByProcessedAtIsNullOrderByCreatedAtAsc();
}
