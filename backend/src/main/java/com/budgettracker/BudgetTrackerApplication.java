package com.budgettracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Budget Tracker Application - Main Entry Point
 *
 * A comprehensive personal finance and budget tracking SaaS application.
 *
 * @author Budget Tracker Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableKafka
public class BudgetTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetTrackerApplication.class, args);
    }
}
