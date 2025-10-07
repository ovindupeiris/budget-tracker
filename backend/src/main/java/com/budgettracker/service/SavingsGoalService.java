package com.budgettracker.service;

import com.budgettracker.entity.SavingsGoal;
import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.GoalStatus;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.SavingsGoalRepository;
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
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserService userService;

    @Transactional
    public SavingsGoal createGoal(UUID userId, SavingsGoal goal) {
        User user = userService.getUserById(userId);
        goal.setUser(user);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setStatus(GoalStatus.ACTIVE);

        goal = savingsGoalRepository.save(goal);
        log.info("Savings goal created: {} for user: {}", goal.getId(), userId);
        return goal;
    }

    @Transactional(readOnly = true)
    public SavingsGoal getGoalById(UUID goalId) {
        return savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", goalId));
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getUserGoals(UUID userId) {
        return savingsGoalRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getActiveGoals(UUID userId) {
        return savingsGoalRepository.findByUserIdAndStatusAndDeletedFalse(userId, GoalStatus.ACTIVE);
    }

    @Transactional
    public SavingsGoal addToGoal(UUID goalId, BigDecimal amount) {
        SavingsGoal goal = getGoalById(goalId);
        goal.addAmount(amount);
        savingsGoalRepository.save(goal);
        log.info("Added {} to goal: {}", amount, goalId);
        return goal;
    }

    @Transactional
    public SavingsGoal updateGoal(UUID goalId, SavingsGoal updates) {
        SavingsGoal goal = getGoalById(goalId);

        if (updates.getName() != null) goal.setName(updates.getName());
        if (updates.getDescription() != null) goal.setDescription(updates.getDescription());
        if (updates.getTargetAmount() != null) goal.setTargetAmount(updates.getTargetAmount());
        if (updates.getTargetDate() != null) goal.setTargetDate(updates.getTargetDate());

        return savingsGoalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(UUID goalId) {
        SavingsGoal goal = getGoalById(goalId);
        goal.softDelete();
        savingsGoalRepository.save(goal);
        log.info("Savings goal deleted: {}", goalId);
    }
}
