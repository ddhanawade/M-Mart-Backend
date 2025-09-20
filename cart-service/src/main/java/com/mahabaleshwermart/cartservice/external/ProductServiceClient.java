package com.mahabaleshwermart.cartservice.external;

import com.mahabaleshwermart.cartservice.external.dto.ApiResponse;
import com.mahabaleshwermart.cartservice.external.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Product Service communication
 */
@FeignClient(name = "product-service", url = "${feign.client.config.product-service.url:http://product-service:8082}", path = "/api/products", configuration = com.mahabaleshwermart.cartservice.config.FeignConfig.class)
public interface ProductServiceClient {
    
    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") String id);
    
    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    ApiResponse<ProductDto> getProductBySku(@PathVariable("sku") String sku);
} 