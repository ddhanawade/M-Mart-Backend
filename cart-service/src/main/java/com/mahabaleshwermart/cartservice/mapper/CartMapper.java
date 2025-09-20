package com.mahabaleshwermart.cartservice.mapper;

import com.mahabaleshwermart.cartservice.dto.CartItemDto;
import com.mahabaleshwermart.cartservice.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for CartItem entity and DTO conversion
 * Replaces MapStruct to avoid annotation processing conflicts
 */
@Component
public class CartMapper {
    
    /**
     * Convert CartItem entity to CartItemDto
     */
    public CartItemDto toDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        return CartItemDto.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .sessionId(cartItem.getSessionId())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productImage(cartItem.getProductImage())
                .productPrice(cartItem.getProductPrice())
                .originalPrice(cartItem.getOriginalPrice())
                .productUnit(cartItem.getProductUnit())
                .quantity(cartItem.getQuantity())
                .selectedQuantity(cartItem.getSelectedQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .available(cartItem.isAvailable())
                .productCategory(cartItem.getProductCategory())
                .productSku(cartItem.getProductSku())
                .organic(cartItem.isOrganic())
                .fresh(cartItem.isFresh())
                .addedAt(cartItem.getAddedAt())
                .createdAt(cartItem.getCreatedAt())
                .build();
    }
    
    /**
     * Convert CartItemDto to CartItem entity
     */
    public CartItem toEntity(CartItemDto cartItemDto) {
        if (cartItemDto == null) {
            return null;
        }
        
        return CartItem.builder()
                .userId(cartItemDto.getUserId())
                .sessionId(cartItemDto.getSessionId())
                .productId(cartItemDto.getProductId())
                .productName(cartItemDto.getProductName())
                .productImage(cartItemDto.getProductImage())
                .productPrice(cartItemDto.getProductPrice())
                .originalPrice(cartItemDto.getOriginalPrice())
                .productUnit(cartItemDto.getProductUnit())
                .quantity(cartItemDto.getQuantity())
                .selectedQuantity(cartItemDto.getSelectedQuantity())
                .totalPrice(cartItemDto.getTotalPrice())
                .available(cartItemDto.isAvailable())
                .productCategory(cartItemDto.getProductCategory())
                .productSku(cartItemDto.getProductSku())
                .organic(cartItemDto.isOrganic())
                .fresh(cartItemDto.isFresh())
                .addedAt(cartItemDto.getAddedAt())
                .active(true)
                .build();
    }
    
    /**
     * Convert list of CartItem entities to list of CartItemDtos
     */
    public List<CartItemDto> toDtoList(List<CartItem> cartItems) {
        if (cartItems == null) {
            return null;
        }
        
        return cartItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert list of CartItemDtos to list of CartItem entities
     */
    public List<CartItem> toEntityList(List<CartItemDto> cartItemDtos) {
        if (cartItemDtos == null) {
            return null;
        }
        
        return cartItemDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 