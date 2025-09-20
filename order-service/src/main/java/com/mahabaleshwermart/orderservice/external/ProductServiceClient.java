package com.mahabaleshwermart.orderservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Product Service communication
 */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductServiceClient {
    
    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    ProductDto getProductById(@PathVariable("id") String id);
}

/**
 * Product DTO for external communication
 */
record ProductDto(
    String id,
    String name,
    String description,
    java.math.BigDecimal price,
    boolean inStock,
    int quantity,
    String unit
) {} 