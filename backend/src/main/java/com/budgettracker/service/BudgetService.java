package com.budgettracker.service;

import com.budgettracker.entity.Budget;
import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.BudgetStatus;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserService userService;

    @Transactional
    public Budget createBudget(UUID userId, Budget budget) {
        User user = userService.getUserById(userId);
        budget.setUser(user);
        budget.setSpent(BigDecimal.ZERO);
        budget.setStatus(BudgetStatus.ACTIVE);

        budget = budgetRepository.save(budget);
        log.info("Budget created: {} for user: {}", budget.getId(), userId);
        return budget;
    }

    @Transactional(readOnly = true)
    public Budget getBudgetById(UUID budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
    }

    @Transactional(readOnly = true)
    public List<Budget> getUserBudgets(UUID userId) {
        return budgetRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Budget> getActiveBudgets(UUID userId) {
        return budgetRepository.findByUserIdAndStatusAndDeletedFalse(userId, BudgetStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Budget> getActiveBudgetsForDate(UUID userId, LocalDate date) {
        return budgetRepository.findActiveBudgetsForDate(userId, date);
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgetsExceedingThreshold(UUID userId) {
        return budgetRepository.findBudgetsExceedingThreshold(userId);
    }

    @Transactional(readOnly = true)
    public List<Budget> getExceededBudgets(UUID userId) {
        return budgetRepository.findExceededBudgets(userId);
    }

    @Transactional
    public Budget updateBudget(UUID budgetId, Budget updates) {
        Budget budget = getBudgetById(budgetId);

        if (updates.getName() != null) budget.setName(updates.getName());
        if (updates.getDescription() != null) budget.setDescription(updates.getDescription());
        if (updates.getAmount() != null) budget.setAmount(updates.getAmount());
        if (updates.getAlertThreshold() != null) budget.setAlertThreshold(updates.getAlertThreshold());
        if (updates.getAlertEnabled() != null) budget.setAlertEnabled(updates.getAlertEnabled());
        if (updates.getRolloverEnabled() != null) budget.setRolloverEnabled(updates.getRolloverEnabled());

        return budgetRepository.save(budget);
    }

    @Transactional
    public void updateBudgetSpending(UUID budgetId, BigDecimal amount) {
        Budget budget = getBudgetById(budgetId);
        budget.updateSpent(amount);

        // Check if alert should be sent
        if (budget.getAlertEnabled() && !budget.getAlertSent() && budget.isAlertThresholdReached()) {
            budget.setAlertSent(true);
            // TODO: Send budget alert notification
            log.info("Budget alert threshold reached for budget: {}", budgetId);
        }

        budgetRepository.save(budget);
    }

    @Transactional
    public void pauseBudget(UUID budgetId) {
        Budget budget = getBudgetById(budgetId);
        budget.setStatus(BudgetStatus.PAUSED);
        budgetRepository.save(budget);
        log.info("Budget paused: {}", budgetId);
    }

    @Transactional
    public void resumeBudget(UUID budgetId) {
        Budget budget = getBudgetById(budgetId);
        budget.setStatus(BudgetStatus.ACTIVE);
        budgetRepository.save(budget);
        log.info("Budget resumed: {}", budgetId);
    }

    @Transactional
    public void deleteBudget(UUID budgetId) {
        Budget budget = getBudgetById(budgetId);
        budget.softDelete();
        budgetRepository.save(budget);
        log.info("Budget deleted: {}", budgetId);
    }

    @Transactional
    public void resetBudgetForNewPeriod(UUID budgetId, LocalDate newStartDate, LocalDate newEndDate) {
        Budget budget = getBudgetById(budgetId);
        budget.resetForNewPeriod(newStartDate, newEndDate);
        budgetRepository.save(budget);
        log.info("Budget reset for new period: {}", budgetId);
    }
}
