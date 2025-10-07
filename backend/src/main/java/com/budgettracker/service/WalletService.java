package com.budgettracker.service;

import com.budgettracker.entity.User;
import com.budgettracker.entity.Wallet;
import com.budgettracker.entity.enums.WalletType;
import com.budgettracker.exception.BusinessException;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;

    @Transactional
    public Wallet createWallet(UUID userId, Wallet wallet) {
        User user = userService.getUserById(userId);
        wallet.setUser(user);

        // If this is the first wallet, set as default
        if (walletRepository.countByUserIdAndDeletedFalse(userId) == 0) {
            wallet.setIsDefault(true);
        }

        wallet = walletRepository.save(wallet);
        log.info("Wallet created: {} for user: {}", wallet.getId(), userId);
        return wallet;
    }

    @Transactional(readOnly = true)
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId));
    }

    @Transactional(readOnly = true)
    public List<Wallet> getUserWallets(UUID userId) {
        return walletRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Wallet> getUserActiveWallets(UUID userId) {
        return walletRepository.findByUserIdAndIsArchivedFalseAndDeletedFalse(userId);
    }

    @Transactional
    public Wallet updateWallet(UUID walletId, Wallet updates) {
        Wallet wallet = getWalletById(walletId);

        if (updates.getName() != null) wallet.setName(updates.getName());
        if (updates.getDescription() != null) wallet.setDescription(updates.getDescription());
        if (updates.getIcon() != null) wallet.setIcon(updates.getIcon());
        if (updates.getColor() != null) wallet.setColor(updates.getColor());
        if (updates.getDisplayOrder() != null) wallet.setDisplayOrder(updates.getDisplayOrder());

        return walletRepository.save(wallet);
    }

    @Transactional
    public void deleteWallet(UUID walletId) {
        Wallet wallet = getWalletById(walletId);
        wallet.softDelete();
        walletRepository.save(wallet);
        log.info("Wallet deleted: {}", walletId);
    }

    @Transactional
    public Wallet setDefaultWallet(UUID userId, UUID walletId) {
        Wallet wallet = getWalletById(walletId);

        if (!wallet.getUser().getId().equals(userId)) {
            throw new BusinessException("Wallet does not belong to user", "UNAUTHORIZED");
        }

        // Remove default from other wallets
        getUserWallets(userId).forEach(w -> {
            if (w.getIsDefault()) {
                w.setIsDefault(false);
                walletRepository.save(w);
            }
        });

        wallet.setIsDefault(true);
        return walletRepository.save(wallet);
    }

    @Transactional
    public void archiveWallet(UUID walletId) {
        Wallet wallet = getWalletById(walletId);
        wallet.archive();
        walletRepository.save(wallet);
        log.info("Wallet archived: {}", walletId);
    }

    @Transactional
    public void unarchiveWallet(UUID walletId) {
        Wallet wallet = getWalletById(walletId);
        wallet.unarchive();
        walletRepository.save(wallet);
        log.info("Wallet unarchived: {}", walletId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(UUID userId) {
        BigDecimal total = walletRepository.calculateTotalBalance(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalanceByCurrency(UUID userId, String currencyCode) {
        BigDecimal total = walletRepository.calculateTotalBalanceByCurrency(userId, currencyCode);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public void updateBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        wallet.updateBalance(amount);
        walletRepository.save(wallet);
        log.info("Wallet balance updated: {} by {}", walletId, amount);
    }
}
