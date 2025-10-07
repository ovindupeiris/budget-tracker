package com.budgettracker.event;

import com.budgettracker.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionCreatedEvent(TransactionCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.TRANSACTION_CREATED_TOPIC,
                event.getTransactionId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction created event published: {}", event.getTransactionId());
            } else {
                log.error("Failed to publish transaction created event: {}", event.getTransactionId(), ex);
            }
        });
    }

    public void publishBudgetAlertEvent(BudgetAlertEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.BUDGET_ALERT_TOPIC,
                event.getBudgetId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Budget alert event published: {}", event.getBudgetId());
            } else {
                log.error("Failed to publish budget alert event: {}", event.getBudgetId(), ex);
            }
        });
    }

    public void publishNotificationEvent(NotificationEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.NOTIFICATION_TOPIC,
                event.getUserId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Notification event published for user: {}", event.getUserId());
            } else {
                log.error("Failed to publish notification event for user: {}", event.getUserId(), ex);
            }
        });
    }
}
