package com.mahabaleshwermart.productservice.controller;

import com.mahabaleshwermart.common.dto.ApiResponse;
import com.mahabaleshwermart.productservice.dto.ProductReviewDto;
import com.mahabaleshwermart.productservice.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Customer reviews for products")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    @Operation(summary = "List reviews", description = "Get reviews for a product")
    public ResponseEntity<ApiResponse<List<ProductReviewDto>>> getReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ProductReviewDto> reviews = reviewService.getReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Reviews retrieved"));
    }

    @PostMapping
    @Operation(summary = "Add review", description = "Submit a review for a product")
    public ResponseEntity<ApiResponse<ProductReviewDto>> addReview(
            @PathVariable String productId,
            @RequestBody ProductReviewDto review) {
        ProductReviewDto saved = reviewService.addReview(productId, review);
        return ResponseEntity.status(201).body(ApiResponse.created(saved, "Review added"));
    }
}


