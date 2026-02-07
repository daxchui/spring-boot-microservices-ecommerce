package com.comp5348.store.repository;

import com.comp5348.store.model.OrderAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAllocationRepository extends JpaRepository<OrderAllocation, Long> {
}
