package com.comp5348.store.repository;

import com.comp5348.store.model.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {

    // find all stock entries for a given product
    List<WarehouseStock> findByProductId(Long productId);

    // find all stock entries for a given warehouse
    List<WarehouseStock> findByWarehouseId(Long warehouseId);

    // find a single stock record for (warehouse, product)
    WarehouseStock findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
