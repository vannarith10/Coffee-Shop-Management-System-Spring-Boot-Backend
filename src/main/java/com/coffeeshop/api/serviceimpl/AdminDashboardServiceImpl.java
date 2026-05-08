package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllStaffProfilesResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.OrderItemRepository;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.util.DateWindows;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {


    private final UserRepository userRepository;
    private final UserService userService;


    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private static final OrderStatus DONE = OrderStatus.DONE;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ImageStorageService imageStorageService;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;


    // ====================================
    // Business Analytics Summary
    // ====================================
    @Override
    public BusinessAnalyticsSummaryResponse businessAnalyticsSummary() {

        // The first time of creating this method is for only ADMIN role, but now
        // I moved validate user role to the Controller place because this function call be called to use at Barista Update Status that has BARISTA role
        // Because Update Order Status handles real-time sending new Business Analytics Summary values
        // that it needs this function to calculate the values and unchanged return type

        ZonedDateTime nowBiz = ZonedDateTime.now(BUSINESS_TZ);
        var today = DateWindows.today(nowBiz);
        var yesterday = DateWindows.yesterday(nowBiz);


        BigDecimal todayRevenue = nvl (orderRepository.sumRevenueBetween(today.getStart(), today.getEnd(), DONE));
        long todayOrders = orderRepository.countOrdersBetween(today.getStart(), today.getEnd(), DONE);

        BigDecimal yRevenue = orderRepository.sumRevenueBetween(yesterday.getStart(), yesterday.getEnd(), DONE);
        long yOrders = orderRepository.countOrdersBetween(yesterday.getStart(), yesterday.getEnd(), DONE);


        BigDecimal todayAov = (todayOrders == 0)
                ? BigDecimal.ZERO
                : todayRevenue.divide(BigDecimal.valueOf(todayOrders), 2, RoundingMode.HALF_UP);

        BigDecimal yAov = (yOrders == 0)
                ? BigDecimal.ZERO
                : yRevenue.divide(BigDecimal.valueOf(yOrders), 2, RoundingMode.HALF_UP);

        double revGrowth = growthPct(todayRevenue, yRevenue);
        double ordGrowth = growthPct(BigDecimal.valueOf(todayOrders), BigDecimal.valueOf(yOrders));
        double aovGrowth = growthPct(todayAov, yAov);



        BusinessAnalyticsSummaryResponse.Summary summary = new BusinessAnalyticsSummaryResponse.Summary(
                new BusinessAnalyticsSummaryResponse.MetricResponse(safeMoney(todayRevenue), revGrowth),
                new BusinessAnalyticsSummaryResponse.MetricResponse((double) todayOrders, ordGrowth),
                new BusinessAnalyticsSummaryResponse.MetricResponse(safeMoney(todayAov), aovGrowth)
        );

        return new BusinessAnalyticsSummaryResponse(summary);
    }



    private static double growthPct(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    // Helper functions
    private static double safeMoney(BigDecimal val) {
        return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }





    // =====================
    // Top Selling Products
    // =====================
    @Override
    public TopSellingProductResponse topSellingProducts(TopSellingProductRequest request) {
        // Validate User
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN role can get this resources.");
        }

        //
        ZoneId zone = ZoneId.systemDefault();
        Instant start;
        Instant end = Instant.now();
        switch (request.range()) {
            case TODAY -> {
                start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
            }
            case THIS_WEEK -> {
                start = LocalDate.now(zone).with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant();
            }
            case THIS_MONTH -> {
                start = LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant();
            }
            case THIS_YEAR -> {
                start = LocalDate.now(zone).withDayOfYear(1).atStartOfDay(zone).toInstant();
            }
            case ALL -> {
                start = Instant.EPOCH;
            }

            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported range");
        }

        // Units target
        final int unitsTarget = 200;

        // Build Pageable
        int page = request.page() != null && request.page() >= 0
                ? request.page()
                : 0;

        // If request.size() < 10 -> return 10
        // If request.size() > 50 -> return 50
        int size = request.size() != null
                ? Math.clamp(request.size(), 10, 50)
                : 10;

        Pageable pageable = PageRequest.of(page, size);


        // Query repository
        Page<TopSellingProductProjection> projections = orderItemRepository
                .findTopSellingProductsByDateRange(
                    OrderStatus.DONE,
                    start,
                    end,
                    pageable
        );

        // Map Projections to response items
        List<TopSellingProductResponse.TopProductItem> productItems = projections.getContent()
                .stream()
                .map(product -> new TopSellingProductResponse.TopProductItem(
                        product.productId(),
                        product.productName(),
                        imageStorageService.getImageUrl(product.imageKey()),
                        Math.toIntExact(product.unitsSold())
                )).toList();

        // Build Response
        TopSellingProductResponse response = TopSellingProductResponse.builder()
                .unitsTarget(unitsTarget)
                .topProducts(productItems)
                .build();

        return response;
    }




    // =====================
    // Product Stock Status
    // =====================
    @Override
    public ProductStockStatusResponse productStockStatus(int page, int size) {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate user Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can get this resources.");
        }

        // Get paginated products
        Page<Product> productPage = productRepository.findAll(PageRequest.of(page - 1, size));

        // Map Product -> Product DTO
        List<ProductStockStatusResponse.ProductItem> productItems = productPage
                .getContent()
                .stream()
                .map(product -> ProductStockStatusResponse.ProductItem.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .categoryName(product.getCategory().getName())
                    .categoryType(product.getCategory().getType())
                    .status(product.getStockStatus())
                .build()).toList();

        ProductStockStatusResponse.Pagination pagination;
        pagination = ProductStockStatusResponse.Pagination.builder()
                .page(page)
                .size(size)
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();


        return ProductStockStatusResponse.builder()
                .message("Products stock statuses")
                .pagination(pagination)
                .products(productItems)
                .build();
    }




    // ===========================
    // Get All Staff Profiles Info
    // ===========================
    @Override
    public GetAllStaffProfilesResponse getAllStaffProfiles(int page, int size) {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        // Validate Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resources.");
        }
        // Get page and size
        int getPage = page > 0 ? page - 1 : 0;
        int getSize = Math.clamp(size, 10, 50);
        Pageable pageable = PageRequest.of(getPage, getSize);
        // Get Paginated of Users
        Page<User> userPage = userRepository.findAll(pageable);
        // Build Pagination response
        GetAllStaffProfilesResponse.Pagination pagination = GetAllStaffProfilesResponse.Pagination.builder()
                .page(getPage + 1)
                .size(getSize)
                .totalPages(userPage.getTotalPages())
                .totalItems(userPage.getTotalElements())
                .build();

        // Build List of Staffs
        List<GetAllStaffProfilesResponse.Staff> staffList = userPage
                .getContent()
                .stream()
                .map(userStaff -> GetAllStaffProfilesResponse.Staff.builder()
                            .id(userStaff.getId())
                            .name(userStaff.getName())
                            .username(userStaff.getUsername())
                            .role(userStaff.getRole())
                            .shift(userStaff.getShiftType())
                            .schedules(userStaff.getSchedules())
                            .email("")
                            .phoneNumber("")
                            .status(userStaff.getStatus())
                            .imageUrl("")
                        .build()).toList();
        return GetAllStaffProfilesResponse.builder()
                .message("Get all staff profiles information")
                .pagination(pagination)
                .staffs(staffList)
                .build();
    }





    // =================
    // ADD NEW EMPLOYEE
    // =================
    @Transactional
    @Override
    public AddNewEmployeeResponse addNewEmployee(AddNewEmployeeRequest request) {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Validate Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resources.");
        }

        // INPUT VALIDATION
        // validate username, must be unique
        if (userRepository.existsByUsername(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // validate password
        if (!request.password().trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weak password");
            // Allow EX: Password@1, Admin@123, Secure!9A
        }

        User newUser = User.builder()
                .name(request.fullName().trim())
                .username(request.username().trim())
                .password(passwordEncoder.encode(request.password().trim()))
                .role(request.role())
                .isActive(true)
                .status(request.status())
                .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant())
                .shiftType(request.shift())
                .schedules(request.schedules())
                .build();
        User saved = userRepository.save(newUser);

        return AddNewEmployeeResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .username(saved.getUsername())
                .role(saved.getRole())
                .shift(saved.getShiftType())
                .schedules(saved.getSchedules())
                .email("")
                .phoneNumber("")
                .status(saved.getStatus())
                .imageUrl("")
                .build();
    }




    // =================
    // Get All Products
    // =================
    @Override
    public GetAllProductsResponse getProducts(int page, int size) {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resource.");
        }
        int pageNumber = page > 0 ? page -1 : 0;
        int pageSize = Math.clamp(size, 10, 50);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Product> products = productRepository.findAll(pageable);

        List<GetAllProductsResponse.ProductItem> productItems = products.getContent()
                .stream()
                .map(product -> GetAllProductsResponse.ProductItem.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .description(product.getDescription())
                        .imageUrl(imageStorageService.getImageUrl(product.getImageKey()))
                        .categoryType(product.getCategory().getType())
                        .categoryName(product.getCategory().getName())
                        .stockStatus(product.getStockStatus())
                        .build())
                .toList();

        GetAllProductsResponse.Pagination pagination = GetAllProductsResponse.Pagination.builder()
                .page(pageNumber + 1)
                .size(pageSize)
                .totalPages(products.getTotalPages())
                .totalItems(products.getTotalElements())
                .build();


        return GetAllProductsResponse.builder()
                .pagination(pagination)
                .productItems(productItems)
                .build();
    }





}



















