package com.budgettracker.service;

import com.budgettracker.entity.Tag;
import com.budgettracker.entity.User;
import com.budgettracker.exception.BusinessException;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserService userService;

    @Transactional
    public Tag createTag(UUID userId, Tag tag) {
        User user = userService.getUserById(userId);

        if (tagRepository.existsByUserIdAndNameAndDeletedFalse(userId, tag.getName())) {
            throw new BusinessException("Tag with this name already exists", "TAG_EXISTS");
        }

        tag.setUser(user);
        tag.setUsageCount(0);

        tag = tagRepository.save(tag);
        log.info("Tag created: {} for user: {}", tag.getId(), userId);
        return tag;
    }

    @Transactional(readOnly = true)
    public Tag getTagById(UUID tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));
    }

    @Transactional(readOnly = true)
    public List<Tag> getUserTags(UUID userId) {
        return tagRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Tag> searchTags(UUID userId, String searchTerm) {
        return tagRepository.findByUserIdAndNameContainingIgnoreCaseAndDeletedFalse(userId, searchTerm);
    }

    @Transactional(readOnly = true)
    public List<Tag> getPopularTags(UUID userId) {
        return tagRepository.findTop10ByUserIdAndDeletedFalseOrderByUsageCountDesc(userId);
    }

    @Transactional
    public Tag updateTag(UUID tagId, Tag updates) {
        Tag tag = getTagById(tagId);

        if (updates.getName() != null) tag.setName(updates.getName());
        if (updates.getColor() != null) tag.setColor(updates.getColor());
        if (updates.getDescription() != null) tag.setDescription(updates.getDescription());

        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = getTagById(tagId);
        tag.softDelete();
        tagRepository.save(tag);
        log.info("Tag deleted: {}", tagId);
    }
}
