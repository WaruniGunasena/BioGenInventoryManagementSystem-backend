package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.Category;
import com.biogenholdings.InventoryMgtSystem.models.Product;
import com.biogenholdings.InventoryMgtSystem.repositories.CategoryRepository;
import com.biogenholdings.InventoryMgtSystem.repositories.ProductRepository;
import com.biogenholdings.InventoryMgtSystem.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-images/";

    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        Product productToSave = Product.builder()
                .name(productDTO.getName().trim())
                .minimumStockLevel(productDTO.getMinimumStockLevel())
                .reorderLevel(productDTO.getReorderLevel())
                .description(productDTO.getDescription())
                .unit(productDTO.getUnit())
                .category(category)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        productRepository.save(productToSave);

        return Response.builder()
                .status(201)
                .message("Product successfully saved")
                .build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            existingProduct.setImageUrl(imagePath);
        }
        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }
        if (productDTO.getName() != null && !productDTO.getName().isEmpty()) {
            existingProduct.setName(productDTO.getName());
        }
        if (productDTO.getMinimumStockLevel() != null && productDTO.getMinimumStockLevel() >= 0) {
            existingProduct.setMinimumStockLevel(productDTO.getMinimumStockLevel());
        }
        if (productDTO.getReorderLevel() != null && productDTO.getReorderLevel() >= 0) {
            existingProduct.setReorderLevel(productDTO.getReorderLevel());
        }
        if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
            existingProduct.setDescription(productDTO.getDescription());
        }

        productRepository.save(existingProduct);

        return Response.builder()
                .status(200)
                .message("Product successfully updated")
                .build();
    }

    @Override
    public Response getAllProducts() {
        List<Product> productList = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProductDTO> productDTOList = modelMapper.map(productList, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .products(productDTOList)
                .build();
    }

    @Override
    public Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        return Response.builder()
                .status(200)
                .message("Success")
                .product(modelMapper.map(product, ProductDTO.class))
                .build();
    }

    @Override
    public Response deleteProduct(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        productRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @Override
    public Response searchProduct(String searchKey) {
        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(searchKey, searchKey);

        if (products.isEmpty()) {
            throw new NotFoundException("Product Not Found");
        }

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .products(productDTOList)
                .build();
    }
    
    private String saveImage(MultipartFile imageFile) {
        try {
            String ext = getString(imageFile);

            Path uploadDir = Paths.get(IMAGE_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Image directory created: {}", uploadDir.toAbsolutePath());
            }

            String uniqueFileName = UUID.randomUUID() + "." + ext;
            Path filePath = uploadDir.resolve(uniqueFileName);

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/product-images/" + uniqueFileName;

        } catch (Exception e) {
            log.error("Image saving failed: {}", e.getMessage());
            throw new RuntimeException("Failed to upload product image");
        }
    }

    private static String getString(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be under 5MB");
        }

        String originalName = imageFile.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("Invalid image file name");
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        List<String> allowedExt = List.of("jpg", "jpeg", "png", "webp");

        if (!allowedExt.contains(ext)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG, WEBP images are allowed");
        }
        return ext;
    }

    @Override
    public Response getPaginatedProducts(Integer page, Integer size, FilterEnum filter) {
        Pageable pageable = PageRequest.of(page,size,getSortByFilter(filter));

        Page<Product> productPage = productRepository.findAll(pageable);

        List<Product> productList = productPage.getContent();

        List<ProductDTO> ProductDTOList = modelMapper.map(productList, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .products(ProductDTOList)
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
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