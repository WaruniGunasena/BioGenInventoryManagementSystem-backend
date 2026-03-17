package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRNItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GRNItemRepository extends JpaRepository<GRNItem, Long> {
    List<GRNItem> findByGrnId(Long grnId);
}