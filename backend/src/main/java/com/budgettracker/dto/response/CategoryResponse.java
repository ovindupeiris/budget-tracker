package com.budgettracker.dto.response;

import com.budgettracker.entity.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private CategoryType type;
    private String icon;
    private String color;
    private Integer displayOrder;
    private UUID parentCategoryId;
    private String parentCategoryName;
    private Boolean isSystem;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
