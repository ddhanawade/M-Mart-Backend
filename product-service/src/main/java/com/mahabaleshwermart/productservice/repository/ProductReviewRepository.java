package com.mahabaleshwermart.productservice.repository;

import com.mahabaleshwermart.productservice.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {

    Page<ProductReview> findByProduct_IdAndActiveTrueOrderByCreatedAtDesc(String productId, Pageable pageable);

    List<ProductReview> findByProduct_IdAndActiveTrue(String productId);

    long countByProduct_IdAndActiveTrue(String productId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ProductReview r WHERE r.product.id = :productId AND r.active = true")
    BigDecimal averageRatingForProduct(@Param("productId") String productId);
}


