package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.ShopProfile;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.AddNewProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import com.coffeeshop.api.dto.adminDashboard.report.ReportDashboardResponse;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopNameAndImage;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.UpdateShopProfileRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.EditStaffRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.*;
import com.coffeeshop.api.service.oldService.AdminDashboardService;
import com.coffeeshop.api.service.oldService.UserService;
import com.coffeeshop.api.util.DateWindows;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final CategoryRepository categoryRepository;
    private final ShopProfileRepository shopProfileRepository;


    // ====================================
    // Business Analytics Summary
    // ====================================
    @Override
    public BusinessAnalyticsSummaryResponse businessAnalyticsSummary() {

        // The first time of creating this method is for only ADMIN role, but now
        // I moved validate user role to the Controller place because this function can be called to use at Barista Update Status that has BARISTA role
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
    // Get All Employee Profiles Info
    // ===========================
    @Override
    public GetAllEmployeeProfilesResponse getAllStaffProfiles(int page, int size) {
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
        GetAllEmployeeProfilesResponse.Pagination pagination = GetAllEmployeeProfilesResponse.Pagination.builder()
                .page(getPage + 1)
                .size(getSize)
                .totalPages(userPage.getTotalPages())
                .totalItems(userPage.getTotalElements())
                .build();

        // Build List of Staffs
        List<GetAllEmployeeProfilesResponse.Employee> staffList = userPage
                .getContent()
                .stream()
                .map(userStaff -> GetAllEmployeeProfilesResponse.Employee.builder()
                            .id(userStaff.getId())
                            .name(userStaff.getName())
                            .username(userStaff.getUsername())
                            .role(userStaff.getRole())
                            .shift(userStaff.getShiftType())
                            .schedules(userStaff.getSchedules())
                            .email("")
                            .phoneNumber("")
                            .status(userStaff.getStatus())
                            .imageUrl(imageStorageService.getImageUrl(userStaff.getImageKey()))
                        .build()).toList();
        return GetAllEmployeeProfilesResponse.builder()
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
    public AddNewEmployeeResponse addNewEmployee(AddNewEmployeeRequest request, MultipartFile image) {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Validate Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resources.");
        }

        // INPUT VALIDATION
        //
        // Full name
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required.");
        }
        // Username
        if (request.username() == null || request.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        // Password
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        // Role
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required.");
        }
        // Shift
        if (request.shift() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shift is required.");
        }
        // Schedules
        if (request.schedules() == null || request.schedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Working days must not be empty.");
        }
        // Status
        if (request.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
        }

        // validate username, must be unique
        if (userRepository.existsByUsername(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // validate password
        if (!request.password().trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weak password");
            // Allow EX: Password@1, Admin@123, Secure!9A
        }

        String imageKey = null;
        if(image != null && !image.isEmpty()){
            imageStorageService.ensureBucketExists();

            String folder = imageStorageService.employeeFolder();
            try {
                imageKey = imageStorageService.upload(image, folder);
            }catch(Exception ex){
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload image to storage",
                        ex
                );
            }
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
                .imageKey(imageKey)
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
                .imageUrl(imageStorageService.getImageUrl(saved.getImageKey()))
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
                        .costPrice(product.getCostPrice())
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



    // ====================
    // Update Stock Status
    // ====================
    @Override
    public void updateProductStockStatus(UUID productId, ProductStock newStockStatus) {
        findUserAndValidateAdminRole();
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
        product.setStockStatus(newStockStatus);
        productRepository.save(product);
    }




    // ===============================
    // [+ -] Update Product Partially
    // ===============================
    @Transactional
    @Override
    public GetAllProductsResponse.ProductItem updateProductPartially(UUID productId, UpdateProductRequest request, MultipartFile file) {
        findUserAndValidateAdminRole();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        if (request.isEmpty() && (file == null || file.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field or image must be provided for update.");
        }

        // Apply name if provided
        if (request.name() != null && !request.name().trim().isEmpty()) {
            product.setName(request.name().trim());
        }

        // Apply new category name if provided
        if (request.categoryName() != null && !request.categoryName().trim().isEmpty()) {
            Category categoryName = categoryRepository.findByNameIgnoreCase(
                    request.categoryName()).orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Category name not found."));
            product.setCategory(categoryName);
        }

        // Apply new selling price if provided
        if (request.sellingPrice() != null && request.sellingPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(request.sellingPrice());
        }

        // Apply new cost price if provided
        if (request.costPrice() != null && request.costPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setCostPrice(request.costPrice());
        }

        // Apply new Description if provided
        if (request.description() != null && !request.description().trim().isEmpty()) {
            product.setDescription(request.description().trim());
        }

        // Update if image exists
        if (file != null && !file.isEmpty()) {
            imageStorageService.ensureBucketExists();

            String categoryFolder = product.getCategory().getName();
            String folder = imageStorageService.buildFolder(categoryFolder);
            String imageKey = null;

            try {
                imageKey = imageStorageService.upload(file, folder); // Returns object key (not a URL)
            } catch (Exception ex) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload image to storage",
                        ex
                );
            }
            product.setImageKey(imageKey);
        }
        product.setUpdatedAt(Instant.now());
        Product p = productRepository.save(product);

        return GetAllProductsResponse.ProductItem.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .description(p.getDescription())
                .imageUrl(imageStorageService.getImageUrl(p.getImageKey()))
                .categoryType(p.getCategory().getType())
                .categoryName(p.getCategory().getName())
                .stockStatus(p.getStockStatus())
                .build();
    }







    // ====================
    // [+] Add New Product
    // ====================
    @Transactional
    @Override
    public GetAllProductsResponse.ProductItem addProduct(AddNewProductRequest request, MultipartFile image) {
        findUserAndValidateAdminRole();

        String name;
        BigDecimal price;
        BigDecimal cost;
        Category category;
        ProductStock stock;
        if(request.name() == null || request.name().trim().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required.");
        }else if(request.sellingPrice() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selling price is required.");
        }else if(request.costPrice() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price is required.");
        }else if(request.categoryName() == null || request.categoryName().trim().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required.");
        }else if(request.stockStatus() == null || request.stockStatus().trim().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock status is required.");
        }else{
            name = request.name().trim();
            price = request.sellingPrice();
            cost = request.costPrice();
            category = categoryRepository.findByNameIgnoreCase(request.categoryName().trim()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category name not found.")
            );
            stock = Arrays.stream(ProductStock.values())
                    .filter(status -> status.name().equalsIgnoreCase(request.stockStatus().trim()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock status: " + request.stockStatus().trim()
                        + " - Available statuses: "
                        + Arrays.stream(ProductStock.values()).map(Enum::name).collect(java.util.stream.Collectors.joining(", "))));
        }

        // Name validation
        if(productRepository.existsByNameIgnoreCase(name)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product name is already exist.");
        }

        // Validate Price
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selling price must be greater than zero.");
        }
        if (cost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price cannot be zero or negative.");
        }
        if (price.compareTo(cost) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selling price must be greater than or equal to cost price."
            );
        }


        String imageKey = null;
        if(image != null && !image.isEmpty()){
            imageStorageService.ensureBucketExists();
            String folder = imageStorageService.buildFolder(category.getName());
            try {
                imageKey = imageStorageService.upload(image, folder);
            }catch(Exception ex){
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to upload image to storage",
                        ex
                );
            }
        }


        Product newProduct = Product.builder()
                .name(name)
                .price(price)
                .costPrice(cost)
                .description(request.description() != null ? request.description().trim() : null)
                .imageKey(imageKey)
                .category(category)
                .stockStatus(stock)
                .available(true)
                .createdAt(Instant.now())
                .build();
        Product saved = productRepository.save(newProduct);


        return GetAllProductsResponse.ProductItem.builder()
                .id(saved.getId())
                .name(saved.getName())
                .price(saved.getPrice())
                .costPrice(saved.getCostPrice())
                .description(saved.getDescription())
                .imageUrl(imageStorageService.getImageUrl(saved.getImageKey()))
                .categoryType(saved.getCategory().getType())
                .categoryName(saved.getCategory().getName())
                .stockStatus(saved.getStockStatus())
                .build();
    }






    //================
    // REPORTS
    //================
    @Override
    public ReportDashboardResponse reports(Integer year, Integer month) {
        findUserAndValidateAdminRole();

        YearMonth getYearMonth;
        if (year != null && month != null) {
            getYearMonth = YearMonth.of(year, month);
        } else {
            getYearMonth = YearMonth.now();
        }
        Instant startOfMonth = getYearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfMonth = getYearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        YearMonth prevMonth = getYearMonth.minusMonths(1);
        Instant startOfPrevMonth = prevMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfPrevMonth = prevMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Summary
        BigDecimal currentRevenue = orderRepository.getTotalRevenue(startOfMonth, endOfMonth);
        BigDecimal prevRevenue = orderRepository.getTotalRevenue(startOfPrevMonth, endOfPrevMonth);

        BigDecimal currentProfit = orderRepository.getGrossProfit(startOfMonth, endOfMonth);
        BigDecimal prevProfit = orderRepository.getGrossProfit(startOfPrevMonth, endOfPrevMonth);

        ReportDashboardResponse.Summary summary = ReportDashboardResponse.Summary.builder()
                .grossProfit(buildMetric(currentProfit, prevProfit))
                .netRevenue(buildMetric(currentRevenue, prevRevenue))
                .build();

        // Revenue Trends
        List<BigDecimal> revenueTrends = buildRevenueTrendsForMonth(getYearMonth, startOfMonth, endOfMonth);

        // Sales by Category
        List<ReportDashboardResponse.CategorySales> salesByCategory = buildSalesByCategory(startOfMonth, endOfMonth, currentRevenue);

        // Busiest Hours
        List<List<Integer>> busiestHours = buildBusiestHours(startOfMonth, endOfMonth);

        return ReportDashboardResponse.builder()
                .summary(summary)
                .revenueTrends(revenueTrends)
                .salesByCategory(salesByCategory)
                .busiesHours(busiestHours)
                .build();
    }

    // Helper -
    private ReportDashboardResponse.Metric buildMetric (BigDecimal current, BigDecimal previous) {
        double growth = calculateGrowthPct(current, previous);
        return ReportDashboardResponse.Metric.builder()
                .value(current.setScale(2, RoundingMode.HALF_UP))
                .growthPtc(growth)
                .build();
    }

    // Helper
    private double calculateGrowthPct (BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    // Helper
    private List<BigDecimal> buildRevenueTrendsForMonth (YearMonth yearMonth, Instant startOfMonth, Instant endOfMonth) {
        List<Object[]> dailyData = orderRepository.getDailyRevenue(startOfMonth, endOfMonth);

        Map<LocalDate, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : dailyData) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(date, revenue);
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        List<BigDecimal> trends = new ArrayList<>();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate current = yearMonth.atDay(1);

        for (int i=1; i<=daysInMonth; i++) {
            BigDecimal value = revenueMap.getOrDefault(current, BigDecimal.ZERO);

            if (current.isAfter(today)) {
                value = BigDecimal.ZERO;
            }

            trends.add(value.setScale(2, RoundingMode.HALF_UP));
            current = current.plusDays(1);
        }
        return trends;
    }

    // Helper
    private List<ReportDashboardResponse.CategorySales> buildSalesByCategory(
            Instant start,
            Instant end,
            BigDecimal totalRevenue) {

        List<Object[]> data = orderRepository.getSalesByCategory(start, end);

        List<ReportDashboardResponse.CategorySales> list = new ArrayList<>();

        for (Object[] row : data) {
            Category category = (Category) row[0];
            String label = category.getName();
            BigDecimal revenue = (BigDecimal) row[1];

            int percentage = (totalRevenue != null && totalRevenue.compareTo(BigDecimal.ZERO) > 0)
                    ? revenue.multiply(BigDecimal.valueOf(100))
                      .divide(totalRevenue, 0, RoundingMode.HALF_UP)
                      .intValue()
                    : 0;

            list.add(ReportDashboardResponse.CategorySales.builder()
                    .label(label)
                    .percentage(percentage)
                    .revenue(revenue.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return list;
    }

    // Helper
    private List<List<Integer>> buildBusiestHours(Instant start, Instant end) {

        List<Object[]> data = orderRepository.getHourlyDistribution(start, end);

        // 7 days (0=Sunday ... 6=Saturday) x 24 hours
        int[][] hours = new int[7][24];

        for (Object[] row : data) {
            int weekday = ((Number) row[0]).intValue();   // EXTRACT(DOW)
            int hour = ((Number) row[1]).intValue();
            int count = ((Number) row[2]).intValue();

            if (weekday >= 0 && weekday < 7 && hour >= 0 && hour < 24) {
                hours[weekday][hour] = count;
            }
        }

        // Convert to List<List<Integer>>
        List<List<Integer>> result = new ArrayList<>();
        for (int[] day : hours) {
            List<Integer> dayList = new ArrayList<>();
            for (int count : day) {
                dayList.add(count);
            }
            result.add(dayList);
        }
        return result;
    }





    // EDIT EMPLOYEE DETAILS
    @Transactional
    @Override
    public GetAllEmployeeProfilesResponse.Employee editStaffDetail(UUID id, EditStaffRequest request, MultipartFile image) {
        findUserAndValidateAdminRole();
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                )
        );
        // Update at least one provided field
        boolean noFields = request == null || (
                request.name() == null &&
                        request.username() == null &&
                        request.password() == null &&
                        request.role() == null &&
                        request.status() == null &&
                        request.shiftType() == null &&
                        request.isActive() == null &&
                        (request.schedules() == null || request.schedules().isEmpty())
        );

        if (noFields && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one field must be provided.");
        }

        // name
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }

        // username
        if (request.username() != null && !request.username().isBlank()) {
            String newUsername = request.username().trim().toLowerCase();

            if (!newUsername.equals(user.getUsername()) &&
                    userRepository.existsByUsername(newUsername)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
            }
            user.setUsername(newUsername);
        }


        // password
        if (request.password() != null && !request.password().isBlank()) {
            if (!request.password().trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weak password");
                // Allow EX: Vannarith#15
            }
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }

        // role
        if (request.role() != null) {
            user.setRole(request.role());
        }

        // is active
        if (request.isActive() != null) {
            user.setActive(request.isActive());
        }


        // status
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        // shift type
        if (request.shiftType() != null) {
            user.setShiftType(request.shiftType());
        }

        // schedules
        if (request.schedules() != null && !request.schedules().isEmpty()){
            user.setSchedules(request.schedules());
        }



        // New image ?
        String oldImageKey = user.getImageKey();
        String imageKey;
        if (image != null && !image.isEmpty()) {
            imageStorageService.ensureBucketExists();
            String folder = imageStorageService.employeeFolder();
            try {
                imageKey = imageStorageService.upload(image, folder);
                user.setImageKey(imageKey);
                if (oldImageKey != null) {
                    imageStorageService.delete(oldImageKey);
                }
            } catch (Exception ex) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error processing image ", ex
                );
            }
        }

        User saved = userRepository.save(user);


        return GetAllEmployeeProfilesResponse.Employee.builder()
                .id(saved.getId())
                .name(saved.getName())
                .username(saved.getUsername())
                .role(saved.getRole())
                .shift(saved.getShiftType())
                .schedules(saved.getSchedules())
                .email("")
                .phoneNumber("")
                .status(saved.getStatus())
                .imageUrl(imageStorageService.getImageUrl(saved.getImageKey()))
                .build();
    }




    // =================
    // GET SHOP PROFILE
    // =================
    @Override
    public GetShopProfile shopProfile() {
        findUserAndValidateAdminRole();
        User admin = userRepository.findById(userService.getCurrentUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found.")
        );
        ShopProfile profile = admin.getShopProfile();
        return GetShopProfile.builder()
                .name(profile.getName())
                .contact(profile.getContactNumber())
                .address(profile.getAddress())
                .description(profile.getDescription())
                .imageUrl(imageStorageService.getImageUrl(profile.getImageKey()))
                .region(profile.getRegion())
                .build();
    }





    // ====================
    // UPDATE SHOP PROFILE
    // ====================
    @Transactional
    @Override
    public GetShopProfile updateShopProfile(UpdateShopProfileRequest request, MultipartFile image) {
        User user = getUserAndValidateAdminRole();
        ShopProfile profile = user.getShopProfile();
        // If no field provided
        boolean noField = request == null ||
                Stream.of(
                        request.name(),
                        request.contact(),
                        request.address(),
                        request.description(),
                        request.region()
                ).allMatch(v -> v == null || v.isBlank());

        if (noField && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Need at least one field provided.");
        }

        if (request != null){
            // name
            if (request.name() != null && !request.name().isBlank()) {
                profile.setName(request.name().trim());
            }

            // contact
            if (request.contact() != null && !request.contact().isBlank()) {
                profile.setContactNumber(request.contact().trim());
            }

            // address
            if (request.address() != null && !request.address().isBlank()) {
                profile.setAddress(request.address().trim());
            }

            // description
            if (request.description() != null && !request.description().isBlank()) {
                profile.setDescription(request.description().trim());
            }

            // region
            if (request.region() != null && !request.region().isBlank()) {
                profile.setRegion(request.region().trim());
            }
        }

        // image
        if (image != null && !image.isEmpty()){
            String oldImageKey = profile.getImageKey();
            String newImageKey;
            String folder = imageStorageService.shopProfileFolder();
            try{
                newImageKey = imageStorageService.upload(image, folder);
                profile.setImageKey(newImageKey);
                if (oldImageKey != null) {
                    try {
                        imageStorageService.delete(oldImageKey);
                    } catch (Exception e) {
                        log.warn("Failed to delete image: {}", oldImageKey, e);
                    }
                }
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error processing image. ", ex);
            }
        }

        return GetShopProfile.builder()
                .name(profile.getName())
                .contact(profile.getContactNumber())
                .address(profile.getAddress())
                .description(profile.getDescription())
                .imageUrl(imageStorageService.getImageUrl(profile.getImageKey()))
                .region(profile.getRegion())
                .build();
    }





    @Override
    public GetShopNameAndImage getShopNameAndImage() {
        Optional<ShopProfile> profile = shopProfileRepository.findFirstByOrderByIdAsc();
        ShopProfile pro = profile.orElse(null);

        return GetShopNameAndImage.builder()
                .name(pro != null && pro.getName() != null ? pro.getName() : "COFFEE")
                .imageUrl(imageStorageService.getImageUrl(pro.getImageKey()))
                .build();
    }





    // Helper - Validate Admin Role
    private void findUserAndValidateAdminRole () {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resource.");
        }
    }
    // I want to return User value
    private User getUserAndValidateAdminRole () {
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resource.");
        }
        return user;
    }
}



















