package com.mahabaleshwermart.productservice.repository;

import com.mahabaleshwermart.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations with advanced search capabilities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    /**
     * Find all active products
     */
    Page<Product> findByActiveTrue(Pageable pageable);
    
    /**
     * Find products by category
     */
    Page<Product> findByCategoryAndActiveTrue(Product.ProductCategory category, Pageable pageable);
    
    /**
     * Find products by category and subcategory
     */
    Page<Product> findByCategoryAndSubcategoryAndActiveTrue(
        Product.ProductCategory category, String subcategory, Pageable pageable);
    
    /**
     * Find featured products
     */
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);
    
    /**
     * Find organic products
     */
    Page<Product> findByOrganicTrueAndActiveTrue(Pageable pageable);
    
    /**
     * Find fresh products
     */
    Page<Product> findByFreshTrueAndActiveTrue(Pageable pageable);
    
    /**
     * Find products in stock
     */
    Page<Product> findByInStockTrueAndActiveTrue(Pageable pageable);
    
    /**
     * Find products by price range
     */
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find products by minimum rating
     */
    Page<Product> findByRatingGreaterThanEqualAndActiveTrue(BigDecimal minRating, Pageable pageable);
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySkuAndActiveTrue(String sku);
    
    /**
     * Find product by barcode
     */
    Optional<Product> findByBarcodeAndActiveTrue(String barcode);
    
    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "p.active = true")
    Page<Product> searchByNameOrDescription(@Param("query") String query, Pageable pageable);
    
    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.farmerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.supplierName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:subcategory IS NULL OR p.subcategory = :subcategory) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:farmerName IS NULL OR LOWER(p.farmerName) = LOWER(:farmerName)) AND " +
           "(:season IS NULL OR LOWER(p.season) = LOWER(:season)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating) AND " +
           "(:inStock IS NULL OR p.inStock = :inStock) AND " +
           "(:organic IS NULL OR p.organic = :organic) AND " +
           "(:fresh IS NULL OR p.fresh = :fresh) AND " +
           "(:featured IS NULL OR p.featured = :featured) AND " +
           "p.active = true")
    Page<Product> searchWithFilters(
        @Param("query") String query,
        @Param("category") Product.ProductCategory category,
        @Param("subcategory") String subcategory,
        @Param("brand") String brand,
        @Param("farmerName") String farmerName,
        @Param("season") String season,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minRating") BigDecimal minRating,
        @Param("inStock") Boolean inStock,
        @Param("organic") Boolean organic,
        @Param("fresh") Boolean fresh,
        @Param("featured") Boolean featured,
        Pageable pageable
    );
    
    /**
     * Find low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantity <= 10 AND p.quantity > 0 AND p.active = true")
    List<Product> findLowStockProducts();
    
    /**
     * Find out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantity = 0 AND p.active = true")
    List<Product> findOutOfStockProducts();
    
    /**
     * Get product count by category
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.active = true GROUP BY p.category")
    List<Object[]> getProductCountByCategory();
    
    /**
     * Find related products (same category, different product)
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :productId AND p.active = true ORDER BY p.rating DESC")
    Page<Product> findRelatedProducts(@Param("category") Product.ProductCategory category, 
                                    @Param("productId") String productId, Pageable pageable);
    
    /**
     * Find top-rated products
     */
    @Query("SELECT p FROM Product p WHERE p.rating >= 4.0 AND p.reviewCount >= 5 AND p.active = true ORDER BY p.rating DESC, p.reviewCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    /**
     * Find best-selling products (based on review count as proxy)
     */
    @Query("SELECT p FROM Product p WHERE p.reviewCount > 0 AND p.active = true ORDER BY p.reviewCount DESC")
    Page<Product> findBestSellingProducts(Pageable pageable);
    
    /**
     * Find products on sale
     */
    @Query("SELECT p FROM Product p WHERE p.originalPrice IS NOT NULL AND p.originalPrice > p.price AND p.active = true")
    Page<Product> findProductsOnSale(Pageable pageable);
    
    /**
     * Update product rating and review count
     */
    @Query("UPDATE Product p SET p.rating = :rating, p.reviewCount = :reviewCount WHERE p.id = :productId")
    void updateProductRating(@Param("productId") String productId, 
                           @Param("rating") BigDecimal rating, 
                           @Param("reviewCount") int reviewCount);
    
    /**
     * Update product stock quantity
     */
    @Query("UPDATE Product p SET p.quantity = :quantity, p.inStock = :inStock WHERE p.id = :productId")
    void updateProductStock(@Param("productId") String productId, 
                          @Param("quantity") int quantity, 
                          @Param("inStock") boolean inStock);
} 