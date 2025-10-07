package com.budgettracker.repository;

import com.budgettracker.entity.RecurringTransaction;
import com.budgettracker.entity.enums.RecurringStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {
    List<RecurringTransaction> findByUserIdAndDeletedFalse(UUID userId);
    List<RecurringTransaction> findByUserIdAndStatusAndDeletedFalse(UUID userId, RecurringStatus status);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.status = 'ACTIVE' " +
           "AND r.autoCreate = true AND r.nextOccurrenceDate <= :date AND r.deleted = false")
    List<RecurringTransaction> findDueForCreation(@Param("date") LocalDate date);
}
