package com.budgettracker.controller;

import com.budgettracker.dto.request.CreateBudgetRequest;
import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.response.BudgetResponse;
import com.budgettracker.entity.Budget;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.Wallet;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.BudgetService;
import com.budgettracker.service.CategoryService;
import com.budgettracker.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management endpoints")
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get all budgets for current user")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Budget> budgets = budgetService.getUserBudgets(currentUser.getId());
        List<BudgetResponse> response = budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active budgets")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getActiveBudgets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Budget> budgets = budgetService.getActiveBudgets(currentUser.getId());
        List<BudgetResponse> response = budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/current")
    @Operation(summary = "Get budgets for current period")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getCurrentBudgets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Budget> budgets = budgetService.getActiveBudgetsForDate(currentUser.getId(), LocalDate.now());
        List<BudgetResponse> response = budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get budgets exceeding alert threshold")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsWithAlerts(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Budget> budgets = budgetService.getBudgetsExceedingThreshold(currentUser.getId());
        List<BudgetResponse> response = budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/exceeded")
    @Operation(summary = "Get exceeded budgets")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getExceededBudgets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<Budget> budgets = budgetService.getExceededBudgets(currentUser.getId());
        List<BudgetResponse> response = budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{budgetId}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @PathVariable UUID budgetId) {
        Budget budget = budgetService.getBudgetById(budgetId);
        return ResponseEntity.ok(ApiResponse.success(toBudgetResponse(budget)));
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateBudgetRequest request) {
        Budget budget = Budget.builder()
                .name(request.getName())
                .description(request.getDescription())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .period(request.getPeriod())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .alertThreshold(request.getAlertThreshold())
                .alertEnabled(request.getAlertEnabled())
                .rolloverEnabled(request.getRolloverEnabled())
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            budget.setCategory(category);
        }

        if (request.getWalletId() != null) {
            Wallet wallet = walletService.getWalletById(request.getWalletId());
            budget.setWallet(wallet);
        }

        Budget created = budgetService.createBudget(currentUser.getId(), budget);
        return ResponseEntity.ok(ApiResponse.success("Budget created successfully", toBudgetResponse(created)));
    }

    @PutMapping("/{budgetId}")
    @Operation(summary = "Update budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateBudgetRequest request) {
        Budget updates = Budget.builder()
                .name(request.getName())
                .description(request.getDescription())
                .amount(request.getAmount())
                .alertThreshold(request.getAlertThreshold())
                .alertEnabled(request.getAlertEnabled())
                .rolloverEnabled(request.getRolloverEnabled())
                .build();

        Budget updated = budgetService.updateBudget(budgetId, updates);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", toBudgetResponse(updated)));
    }

    @PostMapping("/{budgetId}/pause")
    @Operation(summary = "Pause budget")
    public ResponseEntity<ApiResponse<Void>> pauseBudget(@PathVariable UUID budgetId) {
        budgetService.pauseBudget(budgetId);
        return ResponseEntity.ok(ApiResponse.success("Budget paused successfully"));
    }

    @PostMapping("/{budgetId}/resume")
    @Operation(summary = "Resume budget")
    public ResponseEntity<ApiResponse<Void>> resumeBudget(@PathVariable UUID budgetId) {
        budgetService.resumeBudget(budgetId);
        return ResponseEntity.ok(ApiResponse.success("Budget resumed successfully"));
    }

    @DeleteMapping("/{budgetId}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable UUID budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }

    private BudgetResponse toBudgetResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .name(budget.getName())
                .description(budget.getDescription())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .walletId(budget.getWallet() != null ? budget.getWallet().getId() : null)
                .walletName(budget.getWallet() != null ? budget.getWallet().getName() : null)
                .amount(budget.getAmount())
                .spent(budget.getSpent())
                .remaining(budget.getRemaining())
                .percentageUsed(budget.getPercentageSpent())
                .currencyCode(budget.getCurrencyCode())
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .status(budget.getStatus())
                .alertThreshold(budget.getAlertThreshold())
                .alertEnabled(budget.getAlertEnabled())
                .alertSent(budget.getAlertSent())
                .rolloverEnabled(budget.getRolloverEnabled())
                .createdAt(budget.getCreatedAt())
                .build();
    }
}
