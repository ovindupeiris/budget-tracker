package com.budgettracker.repository;

import com.budgettracker.entity.SavingsGoal;
import com.budgettracker.entity.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {
    List<SavingsGoal> findByUserIdAndDeletedFalse(UUID userId);
    List<SavingsGoal> findByUserIdAndStatusAndDeletedFalse(UUID userId, GoalStatus status);

    @Query("SELECT g FROM SavingsGoal g WHERE g.user.id = :userId " +
           "AND g.status = 'ACTIVE' AND g.targetDate < :date AND g.deleted = false")
    List<SavingsGoal> findOverdueGoals(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
