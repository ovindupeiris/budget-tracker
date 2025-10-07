package com.budgettracker.scheduled;

import com.budgettracker.entity.RecurringTransaction;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.enums.TransactionStatus;
import com.budgettracker.repository.RecurringTransactionRepository;
import com.budgettracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;

    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
    public void processRecurringTransactions() {
        log.info("Starting recurring transaction processing");

        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository.findDueForCreation(today);

        log.info("Found {} recurring transactions due for creation", dueTransactions.size());

        for (RecurringTransaction recurring : dueTransactions) {
            try {
                Transaction transaction = Transaction.builder()
                        .user(recurring.getUser())
                        .wallet(recurring.getWallet())
                        .category(recurring.getCategory())
                        .type(recurring.getType())
                        .amount(recurring.getAmount())
                        .currencyCode(recurring.getCurrencyCode())
                        .transactionDate(recurring.getNextOccurrenceDate())
                        .description(recurring.getName())
                        .notes("Auto-created from recurring template")
                        .status(TransactionStatus.COMPLETED)
                        .isRecurring(true)
                        .recurringTemplate(recurring)
                        .build();

                transactionService.createTransaction(recurring.getUser().getId(), transaction);

                recurring.markOccurrenceCreated();
                recurringTransactionRepository.save(recurring);

                log.info("Created transaction from recurring template: {}", recurring.getId());
            } catch (Exception e) {
                log.error("Failed to create transaction from recurring template: {}", recurring.getId(), e);
            }
        }

        log.info("Completed recurring transaction processing");
    }
}
