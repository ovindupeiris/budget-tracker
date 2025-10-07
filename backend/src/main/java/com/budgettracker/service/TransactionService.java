package com.budgettracker.service;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.Wallet;
import com.budgettracker.entity.enums.TransactionType;
import com.budgettracker.exception.BusinessException;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Transactional
    public Transaction createTransaction(UUID userId, Transaction transaction) {
        // Validate wallet belongs to user
        Wallet wallet = walletService.getWalletById(transaction.getWallet().getId());
        if (!wallet.getUser().getId().equals(userId)) {
            throw new BusinessException("Wallet does not belong to user", "UNAUTHORIZED");
        }

        // Set user
        transaction.setUser(wallet.getUser());

        // Update wallet balance
        BigDecimal amount = transaction.getAmount();
        if (transaction.getType() == TransactionType.EXPENSE || transaction.getType() == TransactionType.TRANSFER) {
            amount = amount.negate();
        }

        walletService.updateBalance(wallet.getId(), amount);

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created: {} for user: {}", transaction.getId(), userId);

        return transaction;
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getUserTransactions(UUID userId, Pageable pageable) {
        return transactionRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByDateRange(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return transactionRepository.findByUserAndDateRange(userId, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByWallet(UUID walletId, Pageable pageable) {
        return transactionRepository.findByWalletIdAndDeletedFalse(walletId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByCategory(UUID categoryId, Pageable pageable) {
        return transactionRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> searchTransactions(UUID userId, String searchTerm, Pageable pageable) {
        return transactionRepository.searchTransactions(userId, searchTerm, pageable);
    }

    @Transactional
    public Transaction updateTransaction(UUID transactionId, Transaction updates) {
        Transaction transaction = getTransactionById(transactionId);

        // If amount or type changes, adjust wallet balance
        if (updates.getAmount() != null && !updates.getAmount().equals(transaction.getAmount())) {
            // Revert old amount
            BigDecimal oldAmount = transaction.getAmount();
            if (transaction.getType() == TransactionType.EXPENSE || transaction.getType() == TransactionType.TRANSFER) {
                oldAmount = oldAmount.negate();
            }
            walletService.updateBalance(transaction.getWallet().getId(), oldAmount.negate());

            // Apply new amount
            BigDecimal newAmount = updates.getAmount();
            if (transaction.getType() == TransactionType.EXPENSE || transaction.getType() == TransactionType.TRANSFER) {
                newAmount = newAmount.negate();
            }
            walletService.updateBalance(transaction.getWallet().getId(), newAmount);

            transaction.setAmount(updates.getAmount());
        }

        if (updates.getDescription() != null) transaction.setDescription(updates.getDescription());
        if (updates.getNotes() != null) transaction.setNotes(updates.getNotes());
        if (updates.getTransactionDate() != null) transaction.setTransactionDate(updates.getTransactionDate());
        if (updates.getCategory() != null) transaction.setCategory(updates.getCategory());
        if (updates.getMerchantName() != null) transaction.setMerchantName(updates.getMerchantName());
        if (updates.getLocation() != null) transaction.setLocation(updates.getLocation());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        Transaction transaction = getTransactionById(transactionId);

        // Revert wallet balance
        BigDecimal amount = transaction.getAmount();
        if (transaction.getType() == TransactionType.EXPENSE || transaction.getType() == TransactionType.TRANSFER) {
            amount = amount.negate();
        }
        walletService.updateBalance(transaction.getWallet().getId(), amount.negate());

        transaction.softDelete();
        transactionRepository.save(transaction);
        log.info("Transaction deleted: {}", transactionId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIncome(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateTotalIncome(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalExpenses(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateTotalExpenses(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSpendingByCategory(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateSpendingByCategory(userId, startDate, endDate);
    }

    @Transactional
    public void reconcileTransaction(UUID transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        transaction.reconcile();
        transactionRepository.save(transaction);
        log.info("Transaction reconciled: {}", transactionId);
    }
}
