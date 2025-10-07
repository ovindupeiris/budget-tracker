package com.budgettracker.repository;

import com.budgettracker.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Tag entity
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Find tags by user ID
     */
    List<Tag> findByUserIdAndDeletedFalse(UUID userId);

    /**
     * Find tag by name
     */
    Optional<Tag> findByUserIdAndNameAndDeletedFalse(UUID userId, String name);

    /**
     * Find tags by name containing
     */
    List<Tag> findByUserIdAndNameContainingIgnoreCaseAndDeletedFalse(UUID userId, String searchTerm);

    /**
     * Check if tag name exists
     */
    boolean existsByUserIdAndNameAndDeletedFalse(UUID userId, String name);

    /**
     * Find popular tags (most used)
     */
    List<Tag> findTop10ByUserIdAndDeletedFalseOrderByUsageCountDesc(UUID userId);
}
