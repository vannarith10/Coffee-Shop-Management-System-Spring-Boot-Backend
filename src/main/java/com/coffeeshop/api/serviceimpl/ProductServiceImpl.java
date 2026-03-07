package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.product.*;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.CategoryRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.ProductService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.websocket.ProductEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProductEventPublisher productEventPublisher;


    //====================
    // Add New Product
    //====================
    @Override
    public CreateProductResponse createProduct(String name,
                                               BigDecimal price,
                                               BigDecimal costPrice,
                                               String categoryName,
                                               boolean status,
                                               MultipartFile image) {
        // Validate User (Admin only)
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Check Role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Admin can add new product.");
        }

        // Validate Input
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (price == null || price.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be >= 0");
        }
        if(costPrice == null || costPrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price must be >= 0");
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        // Check if Product Name is already exist
        if(productRepository.existsByNameIgnoreCase(name.trim())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exist");
        }


        // Check Category
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));

        // Ensure the bucket exists before uploads
        imageStorageService.ensureBucketExists();

        // Upload image
        String folder = buildFolder(categoryName);
        String imageKey = null;

        try {
            if (image != null && !image.isEmpty()) {
                imageKey = imageStorageService.upload(image, folder); // Returns object key (not a URL)
            }
        } catch (Exception ex) {
            // // Fail-fast or handle gracefully depending on your UX requirement
            throw new RuntimeException("Failed to upload image to MinIO: " + ex.getMessage(), ex);
        }


        String presignedUrl = null;
        if (imageKey != null) {
            URL url = imageStorageService.getPresignedGetUrl(imageKey);
            presignedUrl = url.toString();
        }


        // Build and save the Product entity
        Product product = Product.builder()
                .name(name.trim())
                .price(price)
                .costPrice(costPrice)
                .imageKey(imageKey)
                .category(category)
                .available(status)
                .createdAt(Instant.now())
                .build();

        product = productRepository.save(product);

        // Send to POS
        productEventPublisher.publish(new ProductEvent(
                "product.added",
                product.getId(),
                MenuItemsResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .imageUrl(presignedUrl)
                        .description(product.getDescription())
                        .categoryType(product.getCategory().getType())
                        .categoryName(product.getCategory().getName())
                        .inStock(product.isAvailable())
                        .build()
        ));


        return CreateProductResponse.builder()
                .status("success")
                .message("Product created successfully.")
                .data(CreateProductResponse.ProductDate
                        .builder()
                        .productId(product.getId())
                        .imageUrl(presignedUrl)
                        .createdAt(product.getCreatedAt())
                        .build())
                .build();
    }//============================================================================



    // Normalizes folder path: products/<category-slug> or products/uncategorized
    private String buildFolder(String categoryName) {
        String slug = slugify(categoryName);
        if (slug.isBlank()) slug = "uncategorized";
        return "products/" + slug;
    }

    // Simple slugifier for folders
    private String slugify(String input) {
        if (input == null) return "";
        String s = input.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("(^-+)|(-+$)", "");
        return s;
    }




    //=================================
    // Get all Product Items to Show
    //=================================
    @Override
    public List<MenuItemsResponse> getMenuItems() {

        // Get User
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate use role
        if(user.getRole() != Role.CASHIER && user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN and CASHIER can get item menu.");
        }

        // Ensure bucket exists once at startup (optional to call here)
        imageStorageService.ensureBucketExists();

        // Get all products
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"));


        // Map to DTOs, generating presigned URL from imageKey
        List<MenuItemsResponse> menuItems = products.stream()
                .map(p -> {
                    String imageKey = p.getImageKey(); // may be null
                    String imageUrl = null;
                    if (imageKey != null && !imageKey.isBlank()) {
                        URL url = imageStorageService.getPresignedGetUrl(imageKey);
                        imageUrl = url.toString();
                    }

                    return MenuItemsResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .price(p.getPrice())
                            .imageUrl(imageUrl) // presigned URL or null
                            .description(p.getDescription())
                            .categoryType(p.getCategory().getType())
                            .categoryName(p.getCategory().getName())
                            .inStock(p.isAvailable())
                            .build();
                })
                .toList();

        return menuItems;
    }//=================================================================================




    // ==================================================================================
    // Patch Product
    // ==================================================================================
    @Override
    public MenuItemsResponse patchProduct(UUID productId, UpdateProductPatchRequest patch) {

        // Get current user and check role
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can update product details.");
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        boolean changed = false;
        Map<String, Object> changeFields = new HashMap<>();

        // Check new name
        if(patch.name() != null && !patch.name().isBlank() && !patch.name().equals(product.getName())){
            product.setName(patch.name());
            changed = true;
            changeFields.put("name", product.getName());
        }

        // Check new price
        if(patch.price() != null){
            if(patch.price().compareTo(BigDecimal.ZERO) <= 0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be greater than ZERO");
            }
            if(product.getPrice() == null || product.getPrice().compareTo(patch.price()) != 0){
                product.setPrice(patch.price());
                changed = true;
                changeFields.put("price", product.getPrice());
            }
        }

        // Check new description
        if(patch.description() != null && !Objects.equals(patch.description(), product.getDescription())){
            product.setDescription(patch.description());
            changed = true;
            changeFields.put("description", product.getDescription());
        }

        // Check new inStock status
        if(patch.inStock() != null && !Objects.equals(patch.inStock(), product.isAvailable())){
            product.setAvailable(patch.inStock());
            changed = true;
            changeFields.put("in_stock", product.isAvailable());
        }

        // Check new Category Name
        if (patch.categoryName() != null && !patch.categoryName().isBlank() && (product.getCategory() == null || !Objects.equals(patch.categoryName().toUpperCase().trim(), product.getCategory().getName()))){
            Category category = categoryRepository.findByNameIgnoreCase(patch.categoryName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

            product.setCategory(category);
            changed = true;
            changeFields.put("category_name", product.getCategory().getName());
        }

        // If all of these fields have not updated
        // We still return, but we return the current fields
        if(!changed){
            String url = product.getImageKey() != null ? imageStorageService.getPresignedGetUrl(product.getImageKey()).toString() : null;
            return MenuItemsResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .imageUrl(url)
                    .description(product.getDescription())
                    .categoryType(product.getCategory().getType())
                    .categoryName(product.getCategory().getName())
                    .inStock(product.isAvailable())
                    .build();
        }

        product.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(product);
        String url = saved.getImageKey() != null ? imageStorageService.getPresignedGetUrl(saved.getImageKey()).toString() : null;

        // WebSocket: product.updated (only changed fields)
        // Send to Cashier
        productEventPublisher.publish(new ProductEvent(
                "product.updated",
                saved.getId(),
                Map.of("changed", changeFields)
        ));


        return MenuItemsResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .price(saved.getPrice())
                .imageUrl(url)
                .description(saved.getDescription())
                .categoryType(saved.getCategory().getType())
                .categoryName(saved.getCategory().getName())
                .inStock(saved.isAvailable())
                .build();
    }//==================================================================================




    // =======================
    // Update Product Image
    // =======================
    @Override
    @Transactional
    public MenuItemsResponse updateProductImage(UUID productId, MultipartFile image) {
        // Check image
        if(image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required.");
        }

        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate Role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN role can update product details.");
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        // Ensure Bucket exists
        imageStorageService.ensureBucketExists();

        // Get Category Name
        Category category = product.getCategory();
        String categoryName = (category != null && category.getName() != null)
                        ? category.getName().trim()
                        : "uncategorized";


        // Upload image
        String folder = buildFolder(categoryName);
        String imageKey = null;

        try{
            imageKey = imageStorageService.upload(image, folder);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image to MinIO.");
        }

        String presignUrl = imageStorageService.getPresignedGetUrl(imageKey).toString();

        // Update product
        product.setImageKey(imageKey);
        product.setUpdatedAt(Instant.now());

        Product saved = productRepository.save(product);

        // Send to POS real-time
        productEventPublisher.publish(new ProductEvent(
                "product.image.updated",
                saved.getId(),
                Map.of(
                        "changed", Map.of("image_url", presignUrl),
                        "updated_at", saved.getUpdatedAt().toString()
                )
        ));

        return MenuItemsResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .price(saved.getPrice())
                .imageUrl(presignUrl)
                .description(saved.getDescription())
                .categoryType(saved.getCategory().getType())
                .categoryName(saved.getCategory().getName())
                .inStock(saved.isAvailable())
                .build();
    }//==================================================================================





    // =======================
    // Update Stock Status
    // =======================
    // TODO
    @Override
    @Transactional
    public void updateStockStatus(UUID productId, boolean abailable) {

        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate User role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can update product details.");
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        // Update fields
        product.setAvailable(abailable);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);

        // WebSocket event
        var event = new ProductEvent(
                "product.stock.updated",
                productId,
                Map.of("available", abailable)
                // { Key : Value }
        );

        // Send to POS instantly
        productEventPublisher.publish(event);

    }//=======================================================================================

    // =================================
    // Update Product Price
    // =================================
    @Override
    @Transactional
    public UpdateProductPriceResponse updateProductPrice(UUID productId, BigDecimal productPrice) {
        // Basic input validation
        if (productPrice == null || productPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be positive");
        }

        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Validate user role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can update product details.");
        }

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        // Update fields
        product.setPrice(productPrice);
        product.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(product);

        // WebSocket Event
        var event = new ProductEvent(
                "product.price.updated",
                productId,
                Map.of("new_price", productPrice)
        );

        // Send to POS
        productEventPublisher.publish(event);

        return new UpdateProductPriceResponse(
                saved.getId(),
                saved.getPrice()
        );
    }//=========================================================================================
}






























