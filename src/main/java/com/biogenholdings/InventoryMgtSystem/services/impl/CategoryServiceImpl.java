package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.CategoryDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.Category;
import com.biogenholdings.InventoryMgtSystem.models.User;
import com.biogenholdings.InventoryMgtSystem.repositories.CategoryRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.UserRepository;
import com.biogenholdings.InventoryMgtSystem.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createCategory(CategoryDTO categoryDTO) {

        Category categoryToSave = modelMapper.map(categoryDTO, Category.class);
        categoryToSave.setIsDeleted(false);

        categoryRepository.save(categoryToSave);

        return Response.builder()
                .status(200)
                .message("Category Saved Successfully")
                .build();
    }

    @Override
    public Response getAllCategory() {

        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        categories.forEach(category -> category.setProducts(null));

        List<CategoryDTO> categoryDTOList = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>()
        {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .categories(categoryDTOList)
                .build();

    }

    @Override
    public Response getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

       return Response.builder()
               .status(200)
               .message("success")
               .category(categoryDTO)
               .build();
    }

    @Override
    public Response updateCategory(Long id, CategoryDTO categoryDTO) {

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        categoryRepository.save(existingCategory);

        return Response.builder()
                .status(200)
                .message("Category was successfully Updated")
                .build();

    }

    @Override
    public Response softDeleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not Found"));

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        category.setIsDeleted(true);
        category.setDeletedBy(user);
        categoryRepository.save(category);

        return Response.builder()
                .status(204)
                .message("Category Deleted Successfully")
                .build();
    }

    @Override
    public Response deleteCategory(Long id) {

        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        categoryRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Category was successfully Deleted")
                .build();

    }

    @Override
    public Response searchCategory(String searchKey) {
        List<Category> categories = categoryRepository.findByNameContainingOrDescriptionContaining(searchKey,searchKey);

        List<CategoryDTO> categoryDTOList = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .categories(categoryDTOList)
                .build();
    }

    @Override
    public Response getPaginatedCategories(Integer page, Integer size,FilterEnum filter) {
        Pageable pageable = PageRequest.of(page,size,getSortByFilter(filter));

        Page<Category> categoryPage = categoryRepository.findByIsDeletedFalse(pageable);

        List<Category> categoryList = categoryPage.getContent();

        List<CategoryDTO> categoryDTOList = modelMapper.map(categoryList, new TypeToken<List<CategoryDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .categories(categoryDTOList)
                .totalPages(categoryPage.getTotalPages())
                .totalElements(categoryPage.getTotalElements())
                .build();

    }

    private Sort getSortByFilter(FilterEnum filter) {
        log.info(filter.toString());
        if (filter == FilterEnum.DESC) {
            return Sort.by(Sort.Direction.DESC, "name");
        } else {
            return Sort.by(Sort.Direction.ASC, "name");
        }
    }

}
