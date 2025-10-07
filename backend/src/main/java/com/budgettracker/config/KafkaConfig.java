package com.budgettracker.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TRANSACTION_CREATED_TOPIC = "transaction.created";
    public static final String TRANSACTION_UPDATED_TOPIC = "transaction.updated";
    public static final String BUDGET_ALERT_TOPIC = "budget.alert";
    public static final String NOTIFICATION_TOPIC = "notification.send";
    public static final String USER_REGISTERED_TOPIC = "user.registered";

    @Bean
    public NewTopic transactionCreatedTopic() {
        return TopicBuilder.name(TRANSACTION_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionUpdatedTopic() {
        return TopicBuilder.name(TRANSACTION_UPDATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic budgetAlertTopic() {
        return TopicBuilder.name(BUDGET_ALERT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
