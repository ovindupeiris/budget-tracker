package com.budgettracker.dto.request;

import com.budgettracker.entity.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    private String icon;
    private String color;
    private Integer displayOrder;
    private UUID parentCategoryId;
    private Boolean isActive = true;
}
