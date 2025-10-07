package com.budgettracker.repository;

import com.budgettracker.entity.Wallet;
import com.budgettracker.entity.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Wallet entity
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID>, JpaSpecificationExecutor<Wallet> {

    /**
     * Find all wallets by user ID
     */
    List<Wallet> findByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find all active (non-archived) wallets by user ID
     */
    List<Wallet> findByUserIdAndIsArchivedFalseAndDeletedFalse(UUID userId);

    /**
     * Find user's default wallet
     */
    Optional<Wallet> findByUserIdAndIsDefaultTrueAndDeletedFalse(UUID userId);

    /**
     * Find wallets by type
     */
    List<Wallet> findByUserIdAndTypeAndDeletedFalse(UUID userId, WalletType type);

    /**
     * Find wallets by currency
     */
    List<Wallet> findByUserIdAndCurrencyCodeAndDeletedFalse(UUID userId, String currencyCode);

    /**
     * Find shared wallets
     */
    List<Wallet> findByUserIdAndIsSharedTrueAndDeletedFalse(UUID userId);

    /**
     * Find wallets with bank sync enabled
     */
    List<Wallet> findByUserIdAndSyncEnabledTrueAndDeletedFalse(UUID userId);

    /**
     * Find wallet by bank connection and account ID
     */
    Optional<Wallet> findByBankConnectionIdAndBankAccountIdAndDeletedFalse(
        String bankConnectionId,
        String bankAccountId
    );

    /**
     * Calculate total balance for user
     */
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId " +
           "AND w.excludeFromTotals = false AND w.deleted = false")
    BigDecimal calculateTotalBalance(@Param("userId") UUID userId);

    /**
     * Calculate total balance by currency
     */
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId " +
           "AND w.currencyCode = :currencyCode AND w.excludeFromTotals = false AND w.deleted = false")
    BigDecimal calculateTotalBalanceByCurrency(
        @Param("userId") UUID userId,
        @Param("currencyCode") String currencyCode
    );

    /**
     * Count user wallets
     */
    long countByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find wallets needing sync
     */
    @Query("SELECT w FROM Wallet w WHERE w.syncEnabled = true " +
           "AND (w.lastSyncedAt IS NULL OR w.lastSyncedAt < :threshold) " +
           "AND w.deleted = false")
    List<Wallet> findWalletsNeedingSync(@Param("threshold") java.time.LocalDateTime threshold);

    /**
     * Find wallets with low balance (for alerts)
     */
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId " +
           "AND w.balance < :threshold AND w.type NOT IN ('CREDIT_CARD', 'LOAN') " +
           "AND w.deleted = false")
    List<Wallet> findWalletsWithLowBalance(
        @Param("userId") UUID userId,
        @Param("threshold") BigDecimal threshold
    );
}
