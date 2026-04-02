package com.biogenholdings.InventoryMgtSystem.repositories;

import com.biogenholdings.InventoryMgtSystem.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingAndIsDeletedFalseOrDescriptionContainingAndIsDeletedFalse(
            String name, String description
    );
    List<Category> findByIsDeletedFalse(Sort sort);
    Page<Category> findByIsDeletedFalse(Pageable pageable);

}
