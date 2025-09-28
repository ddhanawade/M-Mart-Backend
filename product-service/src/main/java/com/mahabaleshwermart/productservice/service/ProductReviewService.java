package com.mahabaleshwermart.productservice.service;

import com.mahabaleshwermart.productservice.dto.ProductReviewDto;
import com.mahabaleshwermart.productservice.entity.Product;
import com.mahabaleshwermart.productservice.entity.ProductReview;
import com.mahabaleshwermart.productservice.repository.ProductRepository;
import com.mahabaleshwermart.productservice.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ProductReviewDto> getReviews(String productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductReview> reviews = reviewRepository.findByProduct_IdAndActiveTrueOrderByCreatedAtDesc(productId, pageable);
        return reviews.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ProductReviewDto addReview(String productId, ProductReviewDto dto) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUserId(dto.getUserId());
        review.setUserName(dto.getUserName());
        review.setRating(dto.getRating() != null ? dto.getRating() : BigDecimal.ZERO);
        review.setComment(dto.getComment());
        review.setTitle(dto.getTitle());
        review.setVerified(false);
        review.setHelpful(false);
        review.setHelpfulCount(0);
        review.setActive(true);

        ProductReview saved = reviewRepository.save(review);

        // Update product aggregates
        updateProductRating(productId);

        return toDto(saved);
    }

    private void updateProductRating(String productId) {
        List<ProductReview> reviews = reviewRepository.findByProduct_IdAndActiveTrue(productId);
        int count = reviews.size();
        BigDecimal avg = BigDecimal.ZERO;
        if (count > 0) {
            BigDecimal total = reviews.stream()
                .map(r -> r.getRating() != null ? r.getRating() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            avg = total.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setReviewCount(count);
        product.setRating(avg);
        productRepository.save(product);
    }

    private ProductReviewDto toDto(ProductReview review) {
        ProductReviewDto dto = new ProductReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setTitle(review.getTitle());
        dto.setVerified(review.isVerified());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}


