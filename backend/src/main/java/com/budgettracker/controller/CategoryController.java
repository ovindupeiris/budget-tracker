package com.budgettracker.controller;

import com.budgettracker.dto.request.CreateCategoryRequest;
import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.response.CategoryResponse;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.enums.CategoryType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories for current user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Category> categories = categoryService.getAllCategoriesForUser(currentUser.getId());
        List<CategoryResponse> response = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByType(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable CategoryType type) {
        List<Category> categories = categoryService.getCategoriesByType(currentUser.getId(), type);
        List<CategoryResponse> response = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/parents")
    @Operation(summary = "Get parent categories only")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getParentCategories(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Category> categories = categoryService.getParentCategories(currentUser.getId());
        List<CategoryResponse> response = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(toCategoryResponse(category)));
    }

    @GetMapping("/{categoryId}/subcategories")
    @Operation(summary = "Get subcategories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(
            @PathVariable UUID categoryId) {
        List<Category> categories = categoryService.getSubcategories(categoryId);
        List<CategoryResponse> response = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setIsActive(request.getIsActive());

        if (request.getParentCategoryId() != null) {
            Category parent = categoryService.getCategoryById(request.getParentCategoryId());
            category.setParentCategory(parent);
        }

        Category created = categoryService.createCategory(currentUser.getId(), category);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", toCategoryResponse(created)));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CreateCategoryRequest request) {
        Category updates = new Category();
        updates.setName(request.getName());
        updates.setDescription(request.getDescription());
        updates.setIcon(request.getIcon());
        updates.setColor(request.getColor());
        updates.setDisplayOrder(request.getDisplayOrder());
        updates.setIsActive(request.getIsActive());

        Category updated = categoryService.updateCategory(categoryId, updates);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", toCategoryResponse(updated)));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .displayOrder(category.getDisplayOrder())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .isSystem(category.getIsSystem())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
