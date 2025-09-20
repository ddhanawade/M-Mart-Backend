package com.mahabaleshwermart.productservice.mapper;

import com.mahabaleshwermart.productservice.dto.ProductDto;
import com.mahabaleshwermart.productservice.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for Product entity and DTO conversion
 * Replaces MapStruct due to annotation processing conflicts
 */
@Component
public class ProductMapper {
    
    /**
     * Convert Product entity to ProductDto
     */
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCategory(categoryToString(product.getCategory()));
        dto.setSubcategory(product.getSubcategory());
        dto.setInStock(product.isInStock());
        dto.setQuantity(product.getQuantity());
        dto.setUnit(product.getUnit());
        dto.setOrganic(product.isOrganic());
        dto.setFresh(product.isFresh());
        dto.setFeatured(product.isFeatured());
        // Note: ProductDto doesn't have active field, skipping
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setDiscount(product.getDiscount());
        dto.setDiscountPercentage(calculateDiscountPercentage(product));
        dto.setOriginCountry(product.getOriginCountry());
        dto.setSupplierName(product.getSupplierName());
        dto.setBarcode(product.getBarcode());
        dto.setImage(product.getImage());
        dto.setWeightKg(product.getWeightKg());
        dto.setShelfLifeDays(product.getShelfLifeDays());
        dto.setStorageInstructions(product.getStorageInstructions());
        dto.setSku(product.getSku());
        dto.setCreatedAt(product.getCreatedAt());
        // Note: ProductDto doesn't have updatedAt field, skipping
        
        if (product.getNutritionalInfo() != null) {
            dto.setNutritionalInfo(mapNutritionalInfo(product.getNutritionalInfo()));
        }
        
        return dto;
    }
    
    /**
     * Convert ProductDto to Product entity
     */
    public Product toEntity(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setOriginalPrice(productDto.getOriginalPrice() != null ? productDto.getOriginalPrice() : productDto.getPrice());
        product.setCategory(stringToCategory(productDto.getCategory()));
        product.setSubcategory(productDto.getSubcategory());
        product.setInStock(productDto.isInStock());
        product.setQuantity(productDto.getQuantity());
        product.setUnit(productDto.getUnit() != null ? productDto.getUnit() : "piece");
        product.setOrganic(productDto.isOrganic());
        product.setFresh(productDto.isFresh());
        product.setFeatured(productDto.isFeatured());
        product.setActive(true);
        product.setRating(productDto.getRating() != null ? productDto.getRating() : BigDecimal.ZERO);
        product.setReviewCount(productDto.getReviewCount());
        product.setDiscount(productDto.getDiscount() != null ? productDto.getDiscount() : BigDecimal.ZERO);
        product.setOriginCountry(productDto.getOriginCountry());
        product.setSupplierName(productDto.getSupplierName());
        product.setBarcode(productDto.getBarcode());
        product.setImage(productDto.getImage());
        product.setWeightKg(productDto.getWeightKg());
        product.setShelfLifeDays(productDto.getShelfLifeDays());
        product.setStorageInstructions(productDto.getStorageInstructions());
        
        // Generate SKU if not provided
        if (productDto.getSku() == null || productDto.getSku().trim().isEmpty()) {
            product.setSku(generateSku(product.getName(), product.getCategory()));
        } else {
            product.setSku(productDto.getSku());
        }
        
        if (productDto.getNutritionalInfo() != null) {
            Product.NutritionalInfo nutritionalInfo = new Product.NutritionalInfo();
            nutritionalInfo.setCaloriesPer100g(productDto.getNutritionalInfo().getCaloriesPer100g());
            nutritionalInfo.setProteinG(productDto.getNutritionalInfo().getProteinG());
            nutritionalInfo.setCarbsG(productDto.getNutritionalInfo().getCarbsG());
            nutritionalInfo.setFatG(productDto.getNutritionalInfo().getFatG());
            nutritionalInfo.setFiberG(productDto.getNutritionalInfo().getFiberG());
            nutritionalInfo.setVitamins(productDto.getNutritionalInfo().getVitamins());
            product.setNutritionalInfo(nutritionalInfo);
        }
        
        return product;
    }
    
    /**
     * Update entity from DTO
     */
    public void updateEntityFromDto(ProductDto productDto, Product product) {
        if (productDto == null || product == null) {
            return;
        }
        
        if (productDto.getName() != null) product.setName(productDto.getName());
        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
        if (productDto.getOriginalPrice() != null) product.setOriginalPrice(productDto.getOriginalPrice());
        if (productDto.getCategory() != null) product.setCategory(stringToCategory(productDto.getCategory()));
        if (productDto.getSubcategory() != null) product.setSubcategory(productDto.getSubcategory());
        product.setInStock(productDto.isInStock());
        product.setQuantity(productDto.getQuantity());
        if (productDto.getUnit() != null) product.setUnit(productDto.getUnit());
        product.setOrganic(productDto.isOrganic());
        product.setFresh(productDto.isFresh());
        product.setFeatured(productDto.isFeatured());
        // Note: ProductDto doesn't have active field, keeping existing value
        if (productDto.getRating() != null) product.setRating(productDto.getRating());
        product.setReviewCount(productDto.getReviewCount());
        if (productDto.getDiscount() != null) product.setDiscount(productDto.getDiscount());
        if (productDto.getOriginCountry() != null) product.setOriginCountry(productDto.getOriginCountry());
        if (productDto.getSupplierName() != null) product.setSupplierName(productDto.getSupplierName());
        if (productDto.getBarcode() != null) product.setBarcode(productDto.getBarcode());
        if (productDto.getImage() != null) product.setImage(productDto.getImage());
        if (productDto.getWeightKg() != null) product.setWeightKg(productDto.getWeightKg());
        if (productDto.getShelfLifeDays() != null) product.setShelfLifeDays(productDto.getShelfLifeDays());
        if (productDto.getStorageInstructions() != null) product.setStorageInstructions(productDto.getStorageInstructions());
        
        if (productDto.getNutritionalInfo() != null) {
            Product.NutritionalInfo nutritionalInfo = product.getNutritionalInfo();
            if (nutritionalInfo == null) {
                nutritionalInfo = new Product.NutritionalInfo();
                product.setNutritionalInfo(nutritionalInfo);
            }
            nutritionalInfo.setCaloriesPer100g(productDto.getNutritionalInfo().getCaloriesPer100g());
            nutritionalInfo.setProteinG(productDto.getNutritionalInfo().getProteinG());
            nutritionalInfo.setCarbsG(productDto.getNutritionalInfo().getCarbsG());
            nutritionalInfo.setFatG(productDto.getNutritionalInfo().getFatG());
            nutritionalInfo.setFiberG(productDto.getNutritionalInfo().getFiberG());
            nutritionalInfo.setVitamins(productDto.getNutritionalInfo().getVitamins());
        }
    }
    
    /**
     * Convert list of Product entities to list of ProductDtos
     */
    public List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    /**
     * Convert list of ProductDtos to list of Product entities
     */
    public List<Product> toEntityList(List<ProductDto> productDtos) {
        if (productDtos == null) {
            return null;
        }
        return productDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
    
    /**
     * Convert Product.ProductCategory enum to String
     */
    public String categoryToString(Product.ProductCategory category) {
        return category != null ? category.name().toLowerCase() : null;
    }
    
    /**
     * Convert String to Product.ProductCategory enum
     */
    public Product.ProductCategory stringToCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        try {
            return Product.ProductCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Calculate discount percentage
     */
    public BigDecimal calculateDiscountPercentage(Product product) {
        if (product.getOriginalPrice() != null && 
            product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0 &&
            product.getPrice() != null) {
            
            BigDecimal difference = product.getOriginalPrice().subtract(product.getPrice());
            return difference.multiply(BigDecimal.valueOf(100))
                    .divide(product.getOriginalPrice(), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Map NutritionalInfo to NutritionalInfoDto
     */
    public ProductDto.NutritionalInfoDto mapNutritionalInfo(Product.NutritionalInfo nutritionalInfo) {
        if (nutritionalInfo == null) {
            return null;
        }
        
        ProductDto.NutritionalInfoDto dto = new ProductDto.NutritionalInfoDto();
        dto.setCaloriesPer100g(nutritionalInfo.getCaloriesPer100g());
        dto.setProteinG(nutritionalInfo.getProteinG());
        dto.setCarbsG(nutritionalInfo.getCarbsG());
        dto.setFatG(nutritionalInfo.getFatG());
        dto.setFiberG(nutritionalInfo.getFiberG());
        dto.setVitamins(nutritionalInfo.getVitamins());
        
        return dto;
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