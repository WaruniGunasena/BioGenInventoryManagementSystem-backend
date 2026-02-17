package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.ProductDTO;
import com.biogenholdings.InventoryMgtSystem.dtos.Response;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-images/";

    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        //map dto to product entity

        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sellingPrice(productDTO.getSellingPrice())
                .minimumStockLevel(productDTO.getMinimumStockLevel())
                .reorderLevel(productDTO.getReorderLevel())
                .description(productDTO.getDescription())
                .category(category)
                .build();

        if (imageFile !=null && !imageFile.isEmpty()){
            log.info("Image file exist");
            String imagePath = saveImage(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        //save the product entity
        productRepository.save(productToSave);

        return Response.builder()
                .status(200)
                .message("Product successfully saved")
                .build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        if (imageFile != null && !imageFile.isEmpty()){
            String imagePath = saveImage(imageFile);
            existingProduct.setImageUrl(imagePath);
        }

        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0){
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }

        //check if product fields is to be changed and update
        if (productDTO.getName() != null && !productDTO.getName().isEmpty()){
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getSellingPrice() != null && productDTO.getSellingPrice().compareTo(BigDecimal.ZERO) >= 0){
            existingProduct.setSellingPrice(productDTO.getSellingPrice());
        }

        if (productDTO.getMinimumStockLevel() != null && productDTO.getMinimumStockLevel() >= 0){
            existingProduct.setMinimumStockLevel(productDTO.getMinimumStockLevel());
        }

        if (productDTO.getReorderLevel() != null && productDTO.getReorderLevel() >= 0){
            existingProduct.setReorderLevel(productDTO.getReorderLevel());
        }

        if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()){
            existingProduct.setDescription(productDTO.getDescription());
        }

        //update the product
        productRepository.save(existingProduct);

        return Response.builder()
                .status(200)
                .message("Product successfully updated")
                .build();
    }

    @Override
    public Response getAllProducts() {

        List<Product> productList = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProductDTO> productDTOList = modelMapper.map(productList, new TypeToken<List<ProductDTO>>()
        {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .products(productDTOList)
                .build();
    }

    @Override
    public Response getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        return Response.builder()
                .status(200)
                .message("success")
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
    public Response searchProduct(String input) {

        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(input, input);

        if (products.isEmpty()){
            throw new NotFoundException("Product Not Found");
        }

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>()
        {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .products(productDTOList)
                .build();
    }

    private String saveImage(MultipartFile imageFile){

        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Only image files under 5MB are allowed");
        }

        //create the directory if it does not exist
        File directory = new File(IMAGE_DIRECTORY);

        if (!directory.exists()){
            directory.mkdir();
            log.info("Directory was created.");
        }

        //generate unique file name for the image
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

        //Get absolute path of the image
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile); // transfer image to this folder
        } catch (Exception e){
            throw new IllegalArgumentException("Error saving Image: " + e.getMessage());
        }

        return imagePath;
    }
}
