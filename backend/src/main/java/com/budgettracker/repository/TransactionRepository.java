package com.budgettracker.repository;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.enums.TransactionStatus;
import com.budgettracker.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    /**
     * Find transactions by user ID with pagination
     */
    Page<Transaction> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    /**
     * Find transactions by wallet ID
     */
    Page<Transaction> findByWalletIdAndDeletedFalse(UUID walletId, Pageable pageable);

    /**
     * Find transactions by category ID
     */
    Page<Transaction> findByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    /**
     * Find transactions by type
     */
    Page<Transaction> findByUserIdAndTypeAndDeletedFalse(UUID userId, TransactionType type, Pageable pageable);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.deleted = false ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserAndDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    /**
     * Find transactions by status
     */
    List<Transaction> findByUserIdAndStatusAndDeletedFalse(UUID userId, TransactionStatus status);

    /**
     * Find unreconciled transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.isReconciled = false AND t.deleted = false")
    List<Transaction> findUnreconciledTransactions(@Param("userId") UUID userId);

    /**
     * Find recurring transactions
     */
    List<Transaction> findByUserIdAndIsRecurringTrueAndDeletedFalse(UUID userId);

    /**
     * Find split transactions
     */
    List<Transaction> findByParentTransactionIdAndDeletedFalse(UUID parentTransactionId);

    /**
     * Find by bank transaction ID
     */
    Optional<Transaction> findByBankTransactionIdAndDeletedFalse(String bankTransactionId);

    /**
     * Find transactions by subscription
     */
    List<Transaction> findBySubscriptionIdAndDeletedFalse(UUID subscriptionId);

    /**
     * Calculate total income for period
     */
    @Query("SELECT COALESCE(SUM(t.amountInWalletCurrency), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'INCOME' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false")
    BigDecimal calculateTotalIncome(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total expenses for period
     */
    @Query("SELECT COALESCE(SUM(t.amountInWalletCurrency), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false")
    BigDecimal calculateTotalExpenses(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate spending by category
     */
    @Query("SELECT t.category.id, SUM(t.amountInWalletCurrency) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false " +
           "GROUP BY t.category.id")
    List<Object[]> calculateSpendingByCategory(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate spending by wallet
     */
    @Query("SELECT t.wallet.id, SUM(t.amountInWalletCurrency) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false " +
           "GROUP BY t.wallet.id")
    List<Object[]> calculateSpendingByWallet(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find recent transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.deleted = false ORDER BY t.transactionDate DESC, t.createdAt DESC")
    Page<Transaction> findRecentTransactions(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Search transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND t.deleted = false")
    Page<Transaction> searchTransactions(
        @Param("userId") UUID userId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    /**
     * Count transactions for user
     */
    long countByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find transactions with attachments
     */
    List<Transaction> findByUserIdAndHasAttachmentsTrueAndDeletedFalse(UUID userId);

    /**
     * Find pending bank transactions
     */
    List<Transaction> findByUserIdAndBankPendingTrueAndDeletedFalse(UUID userId);

    /**
     * Calculate average transaction amount by type
     */
    @Query("SELECT AVG(t.amountInWalletCurrency) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false")
    BigDecimal calculateAverageAmount(
        @Param("userId") UUID userId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find duplicate transactions (for import deduplication)
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.wallet.id = :walletId " +
           "AND t.amount = :amount " +
           "AND t.transactionDate = :date " +
           "AND t.deleted = false")
    List<Transaction> findPotentialDuplicates(
        @Param("userId") UUID userId,
        @Param("walletId") UUID walletId,
        @Param("amount") BigDecimal amount,
        @Param("date") LocalDate date
    );

    /**
     * Get transaction count by month
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', t.transactionDate), COUNT(t) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.deleted = false GROUP BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate)")
    List<Object[]> getTransactionCountByMonth(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Sum transactions by type and date range
     */
    @Query("SELECT COALESCE(SUM(t.amountInWalletCurrency), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false")
    BigDecimal sumByUserIdAndTypeAndDateRange(
        @Param("userId") UUID userId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count transactions by date range
     */
    long countByUserIdAndTransactionDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Sum by category
     */
    @Query("SELECT c.name, COALESCE(SUM(t.amountInWalletCurrency), 0) FROM Transaction t " +
           "LEFT JOIN t.category c WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.status = 'COMPLETED' AND t.deleted = false " +
           "GROUP BY c.name ORDER BY SUM(t.amountInWalletCurrency) DESC")
    List<Object[]> sumByCategory(
        @Param("userId") UUID userId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find top N transactions by user
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.deleted = false ORDER BY t.transactionDate DESC, t.createdAt DESC LIMIT :limit")
    List<Transaction> findTopNByUserId(@Param("userId") UUID userId, @Param("limit") int limit);
}
