package com.budgettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Embeddable class for user preferences
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Column(name = "pref_date_format", length = 20)
    @Builder.Default
    private String dateFormat = "YYYY-MM-DD";

    @Column(name = "pref_time_format", length = 20)
    @Builder.Default
    private String timeFormat = "24H";

    @Column(name = "pref_first_day_of_week")
    @Builder.Default
    private Integer firstDayOfWeek = 1; // 1 = Monday

    @Column(name = "pref_notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "pref_email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "pref_push_notifications")
    @Builder.Default
    private Boolean pushNotifications = false;

    @Column(name = "pref_budget_alerts")
    @Builder.Default
    private Boolean budgetAlerts = true;

    @Column(name = "pref_transaction_alerts")
    @Builder.Default
    private Boolean transactionAlerts = true;

    @Column(name = "pref_bill_reminders")
    @Builder.Default
    private Boolean billReminders = true;

    @Column(name = "pref_theme", length = 20)
    @Builder.Default
    private String theme = "LIGHT";

    @Column(name = "pref_language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "pref_show_balance_on_login")
    @Builder.Default
    private Boolean showBalanceOnLogin = true;

    @Column(name = "pref_biometric_auth_enabled")
    @Builder.Default
    private Boolean biometricAuthEnabled = false;
}
