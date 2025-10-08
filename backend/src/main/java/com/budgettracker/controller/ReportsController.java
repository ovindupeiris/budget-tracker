package com.budgettracker.controller;

import com.budgettracker.dto.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytics and reporting endpoints")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // First day of current month
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> summary = reportsService.getDashboardSummary(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/spending-by-category")
    @Operation(summary = "Get spending breakdown by category")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getSpendingByCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, BigDecimal> spending = reportsService.getSpendingByCategory(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(spending));
    }

    @GetMapping("/income-by-category")
    @Operation(summary = "Get income breakdown by category")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getIncomeByCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, BigDecimal> income = reportsService.getIncomeByCategory(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(income));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get monthly income/expense trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyTrends(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "6") int months) {

        Map<String, Object> trends = reportsService.getMonthlyTrends(currentUser.getId(), months);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/recent-transactions")
    @Operation(summary = "Get recent transactions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentTransactions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {

        List<Map<String, Object>> transactions = reportsService.getRecentTransactions(
                currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
