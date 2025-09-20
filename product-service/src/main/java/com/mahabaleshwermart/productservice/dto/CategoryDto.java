package com.mahabaleshwermart.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class CategoryDto {

    private String id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private String icon;
    private String parentId;
    private List<CategoryDto> subcategories;
    private int sortOrder;
    private boolean featured;
    private long productCount;
    private LocalDateTime createdAt;
}
