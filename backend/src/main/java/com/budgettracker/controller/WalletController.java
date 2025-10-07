package com.budgettracker.controller;

import com.budgettracker.dto.ApiResponse;
import com.budgettracker.dto.request.CreateWalletRequest;
import com.budgettracker.dto.response.WalletResponse;
import com.budgettracker.entity.Wallet;
import com.budgettracker.security.UserPrincipal;
import com.budgettracker.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Wallets", description = "Wallet management endpoints")
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Create a new wallet")
    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateWalletRequest request) {

        Wallet wallet = Wallet.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .currencyCode(request.getCurrencyCode())
                .initialBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .icon(request.getIcon())
                .color(request.getColor())
                .displayOrder(request.getDisplayOrder())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .excludeFromTotals(request.getExcludeFromTotals() != null ? request.getExcludeFromTotals() : false)
                .creditLimit(request.getCreditLimit())
                .billingCycleDay(request.getBillingCycleDay())
                .paymentDueDay(request.getPaymentDueDay())
                .build();

        wallet = walletService.createWallet(userPrincipal.getId(), wallet);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully", mapToResponse(wallet)));
    }

    @Operation(summary = "Get all user wallets")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived) {

        List<Wallet> wallets = includeArchived
                ? walletService.getUserWallets(userPrincipal.getId())
                : walletService.getUserActiveWallets(userPrincipal.getId());

        List<WalletResponse> responses = wallets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Get wallet by ID")
    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable UUID walletId) {

        Wallet wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(wallet)));
    }

    @Operation(summary = "Update wallet")
    @PutMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(
            @PathVariable UUID walletId,
            @RequestBody Wallet updates) {

        Wallet wallet = walletService.updateWallet(walletId, updates);
        return ResponseEntity.ok(ApiResponse.success("Wallet updated successfully", mapToResponse(wallet)));
    }

    @Operation(summary = "Delete wallet")
    @DeleteMapping("/{walletId}")
    public ResponseEntity<ApiResponse<String>> deleteWallet(@PathVariable UUID walletId) {
        walletService.deleteWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success("Wallet deleted successfully", null));
    }

    @Operation(summary = "Set default wallet")
    @PutMapping("/{walletId}/set-default")
    public ResponseEntity<ApiResponse<WalletResponse>> setDefaultWallet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID walletId) {

        Wallet wallet = walletService.setDefaultWallet(userPrincipal.getId(), walletId);
        return ResponseEntity.ok(ApiResponse.success("Default wallet set", mapToResponse(wallet)));
    }

    @Operation(summary = "Archive wallet")
    @PutMapping("/{walletId}/archive")
    public ResponseEntity<ApiResponse<String>> archiveWallet(@PathVariable UUID walletId) {
        walletService.archiveWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success("Wallet archived", null));
    }

    @Operation(summary = "Unarchive wallet")
    @PutMapping("/{walletId}/unarchive")
    public ResponseEntity<ApiResponse<String>> unarchiveWallet(@PathVariable UUID walletId) {
        walletService.unarchiveWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success("Wallet unarchived", null));
    }

    @Operation(summary = "Get total balance")
    @GetMapping("/balance/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBalance(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String currencyCode) {

        BigDecimal total = currencyCode != null
                ? walletService.getTotalBalanceByCurrency(userPrincipal.getId(), currencyCode)
                : walletService.getTotalBalance(userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success(total));
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .description(wallet.getDescription())
                .type(wallet.getType().name())
                .currencyCode(wallet.getCurrencyCode())
                .balance(wallet.getBalance())
                .initialBalance(wallet.getInitialBalance())
                .icon(wallet.getIcon())
                .color(wallet.getColor())
                .displayOrder(wallet.getDisplayOrder())
                .isDefault(wallet.getIsDefault())
                .isArchived(wallet.getIsArchived())
                .isShared(wallet.getIsShared())
                .excludeFromTotals(wallet.getExcludeFromTotals())
                .creditLimit(wallet.getCreditLimit())
                .availableCredit(wallet.getAvailableCredit())
                .syncEnabled(wallet.getSyncEnabled())
                .lastSyncedAt(wallet.getLastSyncedAt())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
