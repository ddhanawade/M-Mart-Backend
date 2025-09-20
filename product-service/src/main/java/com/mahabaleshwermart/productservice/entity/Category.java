package com.mahabaleshwermart.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category entity for organizing products into hierarchical categories
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String slug; // URL-friendly name
    
    @Column(length = 500)
    private String description;
    
    private String image;
    
    private String icon; // Font icon or emoji
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> subcategories;
    
    @Builder.Default
    private int sortOrder = 0;
    
    @Builder.Default
    private boolean active = true;
    
    @Builder.Default
    private boolean featured = false;
    
    @Column(name = "product_count")
    @Builder.Default
    private long productCount = 0;
    
    @Column(name = "meta_title")
    private String metaTitle;
    
    @Column(name = "meta_description")
    private String metaDescription;
    
    @Column(name = "meta_keywords")
    private String metaKeywords;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    @Transient
    public boolean isRootCategory() {
        return parent == null;
    }
    
    @Transient
    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }
    
    @Transient
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }
} 