package com.mahabaleshwermart.productservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.productservice.dto.ProductDto;
import com.mahabaleshwermart.productservice.entity.Product;
import com.mahabaleshwermart.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller
 * Handles product catalog operations, search, and inventory management
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog and search endpoints")
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Get all products with pagination
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all active products with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("Get all products request - page: {}, size: {}, sortBy: {}, direction: {}", 
                page, size, sortBy, sortDirection);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<ProductDto> products = productService.getAllProducts(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Products retrieved successfully")
        );
    }
    
    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable String id) {
        log.info("Get product by ID: {}", id);
        
        ProductDto product = productService.getProductById(id);
        
        return ResponseEntity.ok(
            ApiResponse.success(product, "Product retrieved successfully")
        );
    }
    
    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a specific product by its SKU")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySku(@PathVariable String sku) {
        log.info("Get product by SKU: {}", sku);
        
        ProductDto product = productService.getProductBySku(sku);
        
        return ResponseEntity.ok(
            ApiResponse.success(product, "Product retrieved successfully")
        );
    }
    
    /**
     * Search products with advanced filters
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products with advanced filtering options")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> searchProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String query,
            @Parameter(description = "Product category") @RequestParam(required = false) String category,
            @Parameter(description = "Product subcategory") @RequestParam(required = false) String subcategory,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "In stock only") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Organic products only") @RequestParam(required = false) Boolean organic,
            @Parameter(description = "Fresh products only") @RequestParam(required = false) Boolean fresh,
            @Parameter(description = "Featured products only") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("Search products - query: '{}', category: {}, page: {}", query, category, page);
        
        Product.ProductCategory productCategory = null;
        if (category != null && !category.trim().isEmpty()) {
            try {
                productCategory = Product.ProductCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid category: {}", category);
            }
        }
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<ProductDto> products = productService.searchProducts(
            query, productCategory, subcategory, minPrice, maxPrice, 
            minRating, inStock, organic, fresh, featured, pageable
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Search results retrieved successfully")
        );
    }
    
    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products in a specific category")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("Get products by category: {}", category);
        
        Product.ProductCategory productCategory;
        try {
            productCategory = Product.ProductCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.badRequest("Invalid category: " + category)
            );
        }
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<ProductDto> products = productService.getProductsByCategory(productCategory, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Products retrieved successfully")
        );
    }
    
    /**
     * Get featured products
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieve featured products")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get featured products");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        PageResponse<ProductDto> products = productService.getFeaturedProducts(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Featured products retrieved successfully")
        );
    }
    
    /**
     * Get organic products
     */
    @GetMapping("/organic")
    @Operation(summary = "Get organic products", description = "Retrieve organic products")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getOrganicProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get organic products");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        PageResponse<ProductDto> products = productService.getOrganicProducts(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Organic products retrieved successfully")
        );
    }
    
    /**
     * Get products on sale
     */
    @GetMapping("/sale")
    @Operation(summary = "Get products on sale", description = "Retrieve products with discounts")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProductsOnSale(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get products on sale");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "discountPercentage"));
        PageResponse<ProductDto> products = productService.getProductsOnSale(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Products on sale retrieved successfully")
        );
    }
    
    /**
     * Get top-rated products
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated products", description = "Retrieve highest rated products")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getTopRatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Get top-rated products");
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductDto> products = productService.getTopRatedProducts(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Top-rated products retrieved successfully")
        );
    }
    
    /**
     * Get related products
     */
    @GetMapping("/{id}/related")
    @Operation(summary = "Get related products", description = "Retrieve products related to a specific product")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getRelatedProducts(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        log.info("Get related products for: {}", id);
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductDto> products = productService.getRelatedProducts(id, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Related products retrieved successfully")
        );
    }
    
    /**
     * Get low stock products (Admin only)
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low inventory")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getLowStockProducts() {
        log.info("Get low stock products");
        
        List<ProductDto> products = productService.getLowStockProducts();
        
        return ResponseEntity.ok(
            ApiResponse.success(products, "Low stock products retrieved successfully")
        );
    }
    
    /**
     * Get product count by category
     */
    @GetMapping("/category-counts")
    @Operation(summary = "Get product count by category", description = "Retrieve product counts grouped by category")
    public ResponseEntity<ApiResponse<List<Object[]>>> getProductCountByCategory() {
        log.info("Get product count by category");
        
        List<Object[]> counts = productService.getProductCountByCategory();
        
        return ResponseEntity.ok(
            ApiResponse.success(counts, "Category counts retrieved successfully")
        );
    }
    
    /**
     * Create a new product
     */
    @RequestMapping(method = RequestMethod.POST)
    @Operation(summary = "Create product", description = "Create a new product in the catalog")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto productDto) {
        log.info("Create product request: {}", productDto.getName());
        
        ProductDto createdProduct = productService.createProduct(productDto);
        
        return ResponseEntity.status(201).body(
            ApiResponse.created(createdProduct, "Product created successfully")
        );
    }

    /**
     * Update an existing product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable String id,
            @RequestBody ProductDto productDto) {
        log.info("Update product request: {}", id);
        ProductDto updated = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    /**
     * Delete a product (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft delete a product by id")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String id) {
        log.info("Delete product request: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
    
    /**
     * Test POST endpoint
     */
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    @Operation(summary = "Test POST", description = "Simple test POST endpoint")
    public ResponseEntity<ApiResponse<String>> testPost(@RequestBody String testData) {
        log.info("Test POST endpoint called with data: {}", testData);
        return ResponseEntity.ok(
            ApiResponse.success("POST test successful: " + testData)
        );
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if product service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Product service is running")
        );
    }
} 