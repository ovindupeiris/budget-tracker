package com.budgettracker.service;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.enums.TransactionType;
import com.budgettracker.repository.BudgetRepository;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary(UUID userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        // Total balance across all wallets
        BigDecimal totalBalance = walletRepository.calculateTotalBalance(userId);
        summary.put("totalBalance", totalBalance);

        // Income and expense stats
        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, startDate, endDate);

        summary.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.put("totalExpense", totalExpense != null ? totalExpense : BigDecimal.ZERO);
        summary.put("netIncome", (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO));

        // Transaction counts
        long totalTransactions = transactionRepository.countByUserIdAndTransactionDateBetween(
                userId, startDate, endDate);
        summary.put("totalTransactions", totalTransactions);

        // Active budgets count
        long activeBudgets = budgetRepository.countActiveBudgets(userId);
        summary.put("activeBudgets", activeBudgets);

        // Budgets exceeding threshold
        long budgetsExceedingThreshold = budgetRepository.countBudgetsExceedingThreshold(userId);
        summary.put("budgetsExceedingThreshold", budgetsExceedingThreshold);

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSpendingByCategory(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.sumByCategory(userId, TransactionType.EXPENSE, startDate, endDate);

        Map<String, BigDecimal> spendingByCategory = new LinkedHashMap<>();
        for (Object[] result : results) {
            String categoryName = result[0] != null ? (String) result[0] : "Uncategorized";
            BigDecimal amount = (BigDecimal) result[1];
            spendingByCategory.put(categoryName, amount);
        }

        return spendingByCategory;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getIncomeByCategory(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.sumByCategory(userId, TransactionType.INCOME, startDate, endDate);

        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        for (Object[] result : results) {
            String categoryName = result[0] != null ? (String) result[0] : "Uncategorized";
            BigDecimal amount = (BigDecimal) result[1];
            incomeByCategory.put(categoryName, amount);
        }

        return incomeByCategory;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyTrends(UUID userId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        Map<String, Object> trends = new LinkedHashMap<>();

        for (int i = 0; i < months; i++) {
            YearMonth month = YearMonth.from(endDate.minusMonths(i));
            LocalDate monthStart = month.atDay(1);
            LocalDate monthEnd = month.atEndOfMonth();

            BigDecimal income = transactionRepository.sumByUserIdAndTypeAndDateRange(
                    userId, TransactionType.INCOME, monthStart, monthEnd);
            BigDecimal expense = transactionRepository.sumByUserIdAndTypeAndDateRange(
                    userId, TransactionType.EXPENSE, monthStart, monthEnd);

            Map<String, BigDecimal> monthData = new HashMap<>();
            monthData.put("income", income != null ? income : BigDecimal.ZERO);
            monthData.put("expense", expense != null ? expense : BigDecimal.ZERO);
            monthData.put("net", (income != null ? income : BigDecimal.ZERO)
                    .subtract(expense != null ? expense : BigDecimal.ZERO));

            trends.put(month.toString(), monthData);
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentTransactions(UUID userId, int limit) {
        List<Transaction> transactions = transactionRepository.findTopNByUserId(userId, limit);

        return transactions.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("description", t.getDescription());
                    map.put("amount", t.getAmount());
                    map.put("type", t.getType());
                    map.put("date", t.getTransactionDate());
                    map.put("category", t.getCategory() != null ? t.getCategory().getName() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }
}
