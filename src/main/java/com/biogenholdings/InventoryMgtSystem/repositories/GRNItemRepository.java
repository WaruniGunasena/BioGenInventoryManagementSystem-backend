package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.GRNItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GRNItemRepository extends JpaRepository<GRNItem, Long> {
}