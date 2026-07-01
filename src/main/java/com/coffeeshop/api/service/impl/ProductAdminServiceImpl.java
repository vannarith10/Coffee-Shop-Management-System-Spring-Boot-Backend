package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.dto.adminDashboard.product.AddNewProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.mapper.ProductMapper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.CategoryRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ProductAdminService;
import com.coffeeshop.api.websocket.WebSocketEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdminServiceImpl implements ProductAdminService {


    private final AuthorizationGuard authorizationGuard;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;
    private final WebSocketEventPublisher webSocketEventPublisher;


    //-----------------------
    // GET ALL PRODUCTS
    //-----------------------
    @Override
    public GetAllProductsResponse getAllProducts(int page, int size) {
        authorizationGuard.requireAdmin();

        Pageable pageable = PaginationHelper.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);

        // BUILD LIST OF PRODUCT ITEMS
        List<GetAllProductsResponse.ProductItem> items =
                products.getContent()
                        .stream()
                        .map(productMapper::toProductItemResponseDto)
                        .toList();

        var pagination = Pagination.builder()
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .totalPages(products.getTotalPages())
                .totalItems(products.getTotalElements())
                .build();

        return GetAllProductsResponse
                .builder()
                .pagination(pagination)
                .productItems(items)
                .build();
    }




    //------------------------------------------
    // GET ALL PRODUCT STOCK STATUSES
    //------------------------------------------
    @Override
    public ProductStockStatusResponse GetAllProductStockStatus(int page, int size) {
        authorizationGuard.requireAdmin();

        Pageable pageable = PaginationHelper.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductStockStatusResponse.ProductItem> items =
                productPage.getContent()
                        .stream()
                        .map(productMapper::toProductStockStatusItemResponseDto)
                        .toList();

        var pagination = Pagination.builder()
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();

        return ProductStockStatusResponse.builder()
                .message("Product stock statuses")
                .pagination(pagination)
                .products(items)
                .build();
    }



    //----------------------------
    // ADD NEW PRODUCT ITEM
    //----------------------------
    @Transactional
    @Override
    public GetAllProductsResponse.ProductItem addNewProduct(AddNewProductRequest request, MultipartFile image) {
        authorizationGuard.requireAdmin();

        // Name
        String name = validateName(request.name());
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name already exists.");
        }
        // Price
        BigDecimal price = request.sellingPrice();
        // Cost price
        BigDecimal cost = request.costPrice();

        if (price == null || cost == null || request.categoryName() == null || request.stockStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required fields missing");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0 || cost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prices must be greater than zero");
        }

        if (price.compareTo(cost) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selling price must be greater than or equal to cost price");
        }

        // Category name
        Category category = categoryRepository.findByNameIgnoreCase(request.categoryName().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));

        // Stock status
        ProductStock stock = Arrays.stream(ProductStock.values())
                .filter(s -> s.name().equalsIgnoreCase(request.stockStatus().trim()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock status"));

        // Image
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product image required");
        }
        String imageKey = uploadProductImage(image, imageStorageService.buildFolder(category.getName()));

        // BUILD PRODUCT
        Product product = Product.builder()
                .name(name)
                .price(price)
                .costPrice(cost)
                .description(request.description() != null ? request.description().trim() : null )
                .imageKey(imageKey)
                .category(category)
                .stockStatus(stock)
                .available(true)
                .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant())
                .build();

        return productMapper.toProductItemResponseDto(productRepository.save(product));
    }




    //----------------------------------
    // UPDATE PRODUCT PARTIALLY
    //----------------------------------
    @Transactional
    @Override
    public GetAllProductsResponse.ProductItem updateProductPartially(UUID id, UpdateProductRequest request, MultipartFile image) {
        authorizationGuard.requireAdmin();

        Product product = findProductById(id);

        if (request.isEmpty() && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field or image must be provided.");
        }

        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name().trim());
        }

        if (request.categoryName() != null && !request.categoryName().isBlank()) {
            Category cat = categoryRepository.findByNameIgnoreCase(request.categoryName().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));
            product.setCategory(cat);
        }

        if (request.sellingPrice() != null && request.sellingPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(request.sellingPrice());
        }

        if (request.costPrice() != null && request.costPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setCostPrice(request.costPrice());
        }

        if (request.description() != null && !request.description().isBlank()) {
            product.setDescription(request.description().trim());
        }

        if (image != null && !image.isEmpty()) {
            String key = uploadProductImage(image, imageStorageService.buildFolder(product.getCategory().getName()));
            product.setImageKey(key);
        }

        product.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant());

        return productMapper.toProductItemResponseDto(productRepository.save(product));
    }




    //--------------------------------------
    // UPDATE PRODUCT STOCK STATUS
    //--------------------------------------
    @Override
    public void updateProductStockStatus(UUID id, ProductStock newStockStatus) {
        authorizationGuard.requireAdmin();
        Product product = findProductById(id);

        product.setStockStatus(newStockStatus);
        product.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant());
        Product saved = productRepository.save(product);

        // WebSocket
        var response = productMapper.toProductStockStatusItemResponseDto(saved);
        webSocketEventPublisher.publishProductStockUpdateToAdmins(response);
    }





    // VALIDATE NAME
    private String validateName(String name) {
        if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name required.");
        return name.trim();
    }

    // UPLOAD PRODUCT IMAGE
    private String uploadProductImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        imageStorageService.ensureBucketExists();
        try {
            return imageStorageService.upload(file, folder);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image.", ex);
        }
    }

    // FIND PRODUCT BY ID
    private Product findProductById (UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found"));
    }

}
