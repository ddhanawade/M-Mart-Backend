package com.mahabaleshwermart.productservice.service;

import com.mahabaleshwermart.common.dto.PageResponse;
import com.mahabaleshwermart.common.exception.ResourceNotFoundException;
import com.mahabaleshwermart.productservice.dto.ProductDto;
import com.mahabaleshwermart.productservice.entity.Product;
import com.mahabaleshwermart.productservice.mapper.ProductMapper;
import com.mahabaleshwermart.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Service
 * Handles product catalog operations, search, and inventory management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    /**
     * Get all products with pagination
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getAllProducts(Pageable pageable) {
        log.info("Fetching all products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get product by ID
     */
    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductDto getProductById(String id) {
        log.info("Fetching product by ID: {}", id);
        
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
                
        return productMapper.toDto(product);
    }
    
    /**
     * Get product by SKU
     */
    @Cacheable(value = "product-sku", key = "#sku")
    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        log.info("Fetching product by SKU: {}", sku);
        
        Product product = productRepository.findBySkuAndActiveTrue(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
                
        return productMapper.toDto(product);
    }
    
    /**
     * Search products with advanced filters
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(
            String query,
            Product.ProductCategory category,
            String subcategory,
            String brand,
            String farmerName,
            String season,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating,
            Boolean inStock,
            Boolean organic,
            Boolean fresh,
            Boolean featured,
            Pageable pageable) {
        
        log.info("Searching products with query: '{}', category: {}", query, category);
        
        Page<Product> products = productRepository.searchWithFilters(
            query, category, subcategory, brand, farmerName, season, 
            minPrice, maxPrice, minRating, inStock, organic, fresh, featured, pageable
        );
        
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get products by category
     */
    @Cacheable(value = "products-category", key = "#category + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByCategory(Product.ProductCategory category, Pageable pageable) {
        log.info("Fetching products by category: {}", category);
        
        Page<Product> products = productRepository.findByCategoryAndActiveTrue(category, pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get featured products
     */
    @Cacheable(value = "featured-products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getFeaturedProducts(Pageable pageable) {
        log.info("Fetching featured products");
        
        Page<Product> products = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get organic products
     */
    @Cacheable(value = "organic-products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getOrganicProducts(Pageable pageable) {
        log.info("Fetching organic products");
        
        Page<Product> products = productRepository.findByOrganicTrueAndActiveTrue(pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get products on sale
     */
    @Cacheable(value = "sale-products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsOnSale(Pageable pageable) {
        log.info("Fetching products on sale");
        
        Page<Product> products = productRepository.findProductsOnSale(pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get top-rated products
     */
    @Cacheable(value = "top-rated-products", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getTopRatedProducts(Pageable pageable) {
        log.info("Fetching top-rated products");
        
        Page<Product> products = productRepository.findTopRatedProducts(pageable);
        List<ProductDto> productDtos = productMapper.toDtoList(products.getContent());
        
        return PageResponse.of(
            productDtos,
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages()
        );
    }
    
    /**
     * Get related products
     */
    @Cacheable(value = "related-products", key = "#productId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getRelatedProducts(String productId, Pageable pageable) {
        log.info("Fetching related products for product: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Page<Product> relatedProducts = productRepository.findRelatedProducts(
            product.getCategory(), productId, pageable
        );
        
        List<ProductDto> productDtos = productMapper.toDtoList(relatedProducts.getContent());
        
        return PageResponse.of(
            productDtos,
            relatedProducts.getNumber(),
            relatedProducts.getSize(),
            relatedProducts.getTotalElements(),
            relatedProducts.getTotalPages()
        );
    }
    
    /**
     * Create new product
     */
    @CacheEvict(value = {"products", "products-category", "featured-products"}, allEntries = true)
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        log.info("Creating new product: {}", productDto.getName());
        
        Product product = productMapper.toEntity(productDto);
        product = productRepository.save(product);
        
        log.info("Product created successfully with ID: {}", product.getId());
        return productMapper.toDto(product);
    }
    
    /**
     * Update product
     */
    @CacheEvict(value = {"product", "products", "products-category", "featured-products"}, allEntries = true)
    @Transactional
    public ProductDto updateProduct(String id, ProductDto productDto) {
        log.info("Updating product: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Update fields
        productMapper.updateEntityFromDto(productDto, existingProduct);
        existingProduct = productRepository.save(existingProduct);
        
        log.info("Product updated successfully: {}", id);
        return productMapper.toDto(existingProduct);
    }
    
    /**
     * Delete product (soft delete)
     */
    @CacheEvict(value = {"product", "products", "products-category", "featured-products"}, allEntries = true)
    @Transactional
    public void deleteProduct(String id) {
        log.info("Deleting product: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        product.setActive(false);
        productRepository.save(product);
        
        log.info("Product deleted successfully: {}", id);
    }
    
    /**
     * Update product stock
     */
    @CacheEvict(value = "product", key = "#productId")
    @Transactional
    public void updateProductStock(String productId, int quantity) {
        log.info("Updating stock for product: {} to quantity: {}", productId, quantity);
        
        boolean inStock = quantity > 0;
        productRepository.updateProductStock(productId, quantity, inStock);
        
        log.info("Product stock updated successfully: {}", productId);
    }
    
    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getLowStockProducts() {
        log.info("Fetching low stock products");
        
        List<Product> products = productRepository.findLowStockProducts();
        return productMapper.toDtoList(products);
    }
    
    /**
     * Get product count by category
     */
    @Cacheable(value = "category-counts")
    @Transactional(readOnly = true)
    public List<Object[]> getProductCountByCategory() {
        log.info("Fetching product count by category");
        
        return productRepository.getProductCountByCategory();
    }

    /**
     * Get products by a list of IDs
     */
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Product> products = productRepository.findAllById(ids).stream()
                .filter(Product::isActive)
                .toList();
        return productMapper.toDtoList(products);
    }

    /**
     * Generate SKU for product
     */
    private String generateSku(String name, Product.ProductCategory category) {
        String categoryPrefix = category != null ? category.name().substring(0, 3) : "GEN";
        String namePrefix = name != null && name.length() >= 3 ? 
            name.replaceAll("[^A-Za-z0-9]", "").toUpperCase().substring(0, Math.min(3, name.length())) : "PRD";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        
        return categoryPrefix + "-" + namePrefix + "-" + timestamp;
    }
} 