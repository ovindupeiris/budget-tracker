package com.budgettracker.service;

import com.budgettracker.entity.Subscription;
import com.budgettracker.entity.User;
import com.budgettracker.entity.enums.SubscriptionStatus;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;

    @Transactional
    public Subscription createSubscription(UUID userId, Subscription subscription) {
        User user = userService.getUserById(userId);
        subscription.setUser(user);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        // Calculate next billing date if not provided
        if (subscription.getNextBillingDate() == null) {
            subscription.setNextBillingDate(subscription.calculateNextBillingDate());
        }

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: {} for user: {}", subscription.getId(), userId);
        return subscription;
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
    }

    @Transactional(readOnly = true)
    public List<Subscription> getUserSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserIdAndDeletedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getActiveSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserIdAndStatusAndDeletedFalse(userId, SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsDueForBilling(UUID userId, LocalDate date) {
        return subscriptionRepository.findSubscriptionsDueForBilling(userId, date);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsNeedingReminders(UUID userId, LocalDate startDate, LocalDate endDate) {
        return subscriptionRepository.findSubscriptionsNeedingReminders(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getTrialsEndingSoon(UUID userId, LocalDate startDate, LocalDate endDate) {
        return subscriptionRepository.findTrialsEndingSoon(userId, startDate, endDate);
    }

    @Transactional
    public Subscription updateSubscription(UUID subscriptionId, Subscription updates) {
        Subscription subscription = getSubscriptionById(subscriptionId);

        if (updates.getName() != null) subscription.setName(updates.getName());
        if (updates.getDescription() != null) subscription.setDescription(updates.getDescription());
        if (updates.getAmount() != null) subscription.setAmount(updates.getAmount());
        if (updates.getBillingFrequency() != null) subscription.setBillingFrequency(updates.getBillingFrequency());
        if (updates.getReminderDaysBefore() != null) subscription.setReminderDaysBefore(updates.getReminderDaysBefore());
        if (updates.getReminderEnabled() != null) subscription.setReminderEnabled(updates.getReminderEnabled());

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.cancel();
        subscriptionRepository.save(subscription);
        log.info("Subscription cancelled: {}", subscriptionId);
    }

    @Transactional
    public void pauseSubscription(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.pause();
        subscriptionRepository.save(subscription);
        log.info("Subscription paused: {}", subscriptionId);
    }

    @Transactional
    public void resumeSubscription(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.resume();
        subscriptionRepository.save(subscription);
        log.info("Subscription resumed: {}", subscriptionId);
    }

    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.softDelete();
        subscriptionRepository.save(subscription);
        log.info("Subscription deleted: {}", subscriptionId);
    }

    @Transactional
    public void processSubscriptionBilling(UUID subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setLastBillingDate(subscription.getNextBillingDate());
        subscription.setNextBillingDate(subscription.calculateNextBillingDate());
        subscriptionRepository.save(subscription);
        log.info("Subscription billing processed: {}", subscriptionId);
    }
}
