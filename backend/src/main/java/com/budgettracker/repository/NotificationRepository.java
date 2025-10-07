package com.budgettracker.repository;

import com.budgettracker.entity.Notification;
import com.budgettracker.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' " +
           "AND (n.scheduledFor IS NULL OR n.scheduledFor <= :now)")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);

    long countByUserIdAndIsReadFalse(UUID userId);
}
