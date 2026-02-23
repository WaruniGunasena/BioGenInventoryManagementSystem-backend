package com.biogenholdings.InventoryMgtSystem.services;

import com.biogenholdings.InventoryMgtSystem.dtos.CategoryDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;

public interface CategoryService {

    Response createCategory(CategoryDTO categoryDTO);

    Response getAllCategory();

    Response getCategoryById(Long id);

    Response updateCategory(Long id, CategoryDTO categoryDTO);

    Response deleteCategory(Long id);

    Response softDeleteCategory(Long id, Long userId);

    Response searchCategory(String searchKey);

    Response getPaginatedCategories(Integer page, Integer size, FilterEnum filter);

}
