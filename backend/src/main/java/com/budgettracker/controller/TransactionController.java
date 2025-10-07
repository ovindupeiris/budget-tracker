package com.budgettracker.controller;

import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.PageResponse;
import com.budgettracker.dto.request.CreateTransactionRequest;
import com.budgettracker.dto.response.TransactionResponse;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.Wallet;
import com.budgettracker.entity.enums.TransactionStatus;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.CategoryService;
import com.budgettracker.service.TransactionService;
import com.budgettracker.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Transactions", description = "Transaction management endpoints")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class TransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;
    private final CategoryService categoryService;

    @Operation(summary = "Create a new transaction")
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateTransactionRequest request) {

        Wallet wallet = walletService.getWalletById(request.getWalletId());

        Transaction.TransactionBuilder transactionBuilder = Transaction.builder()
                .wallet(wallet)
                .type(request.getType())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .exchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE)
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .notes(request.getNotes())
                .merchantName(request.getMerchantName())
                .location(request.getLocation())
                .status(TransactionStatus.COMPLETED);

        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            transactionBuilder.category(category);
        }

        Transaction transaction = transactionBuilder.build();
        transaction = transactionService.createTransaction(userPrincipal.getId(), transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", mapToResponse(transaction)));
    }

    @Operation(summary = "Get user transactions with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {

        Page<Transaction> transactionsPage = transactionService.getUserTransactions(userPrincipal.getId(), pageable);
        Page<TransactionResponse> responsePage = transactionsPage.map(this::mapToResponse);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responsePage)));
    }

    @Operation(summary = "Get transaction by ID")
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable UUID transactionId) {

        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(transaction)));
    }

    @Operation(summary = "Get transactions by date range")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByDateRange(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<Transaction> transactionsPage = transactionService.getTransactionsByDateRange(
                userPrincipal.getId(), startDate, endDate, pageable);
        Page<TransactionResponse> responsePage = transactionsPage.map(this::mapToResponse);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responsePage)));
    }

    @Operation(summary = "Search transactions")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> searchTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String query,
            Pageable pageable) {

        Page<Transaction> transactionsPage = transactionService.searchTransactions(
                userPrincipal.getId(), query, pageable);
        Page<TransactionResponse> responsePage = transactionsPage.map(this::mapToResponse);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(responsePage)));
    }

    @Operation(summary = "Update transaction")
    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable UUID transactionId,
            @RequestBody Transaction updates) {

        Transaction transaction = transactionService.updateTransaction(transactionId, updates);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", mapToResponse(transaction)));
    }

    @Operation(summary = "Delete transaction")
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<String>> deleteTransaction(@PathVariable UUID transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }

    @Operation(summary = "Reconcile transaction")
    @PutMapping("/{transactionId}/reconcile")
    public ResponseEntity<ApiResponse<String>> reconcileTransaction(@PathVariable UUID transactionId) {
        transactionService.reconcileTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction reconciled", null));
    }

    @Operation(summary = "Get total income for period")
    @GetMapping("/stats/income")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalIncome(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal total = transactionService.calculateTotalIncome(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @Operation(summary = "Get total expenses for period")
    @GetMapping("/stats/expenses")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalExpenses(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal total = transactionService.calculateTotalExpenses(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .currencyCode(transaction.getCurrencyCode())
                .exchangeRate(transaction.getExchangeRate())
                .amountInWalletCurrency(transaction.getAmountInWalletCurrency())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .notes(transaction.getNotes())
                .merchantName(transaction.getMerchantName())
                .location(transaction.getLocation())
                .status(transaction.getStatus().name())
                .isReconciled(transaction.getIsReconciled())
                .hasAttachments(transaction.getHasAttachments())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
