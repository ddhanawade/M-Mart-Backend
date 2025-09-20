package com.mahabaleshwermart.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Review Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductReviewDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String userName;
    private BigDecimal rating;
    private String comment;
    private String title;
    private boolean verified;
    private int helpfulCount;
    private LocalDateTime createdAt;
}
