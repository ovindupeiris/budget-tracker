package com.budgettracker.service;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.CategoryType;
import com.budgettracker.exception.BusinessException;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional
    public Category createCategory(UUID userId, Category category) {
        User user = userService.getUserById(userId);

        // Check if category name already exists for user
        if (categoryRepository.existsByUserIdAndNameAndDeletedFalse(userId, category.getName())) {
            throw new BusinessException("Category with this name already exists", "CATEGORY_EXISTS");
        }

        category.setUser(user);
        category.setIsSystem(false);
        category = categoryRepository.save(category);

        log.info("Category created: {} for user: {}", category.getId(), userId);
        return category;
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesForUser(UUID userId) {
        return categoryRepository.findAllCategoriesForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesByType(UUID userId, CategoryType type) {
        return categoryRepository.findByUserIdAndTypeAndDeletedFalse(userId, type);
    }

    @Transactional(readOnly = true)
    public List<Category> getParentCategories(UUID userId) {
        return categoryRepository.findByUserIdAndParentCategoryIdIsNullAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Category> getSubcategories(UUID parentCategoryId) {
        return categoryRepository.findByParentCategoryIdAndDeletedFalse(parentCategoryId);
    }

    @Transactional
    public Category updateCategory(UUID categoryId, Category updates) {
        Category category = getCategoryById(categoryId);

        if (category.getIsSystem()) {
            throw new BusinessException("Cannot modify system categories", "SYSTEM_CATEGORY");
        }

        if (updates.getName() != null) category.setName(updates.getName());
        if (updates.getDescription() != null) category.setDescription(updates.getDescription());
        if (updates.getIcon() != null) category.setIcon(updates.getIcon());
        if (updates.getColor() != null) category.setColor(updates.getColor());
        if (updates.getDisplayOrder() != null) category.setDisplayOrder(updates.getDisplayOrder());
        if (updates.getIsActive() != null) category.setIsActive(updates.getIsActive());

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = getCategoryById(categoryId);

        if (category.getIsSystem()) {
            throw new BusinessException("Cannot delete system categories", "SYSTEM_CATEGORY");
        }

        category.softDelete();
        categoryRepository.save(category);
        log.info("Category deleted: {}", categoryId);
    }
}
