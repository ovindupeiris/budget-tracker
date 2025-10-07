package com.budgettracker.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID userId;
    private String type;
    private String title;
    private String message;
    private String channel; // EMAIL, PUSH, SMS, IN_APP
    private String actionUrl;
}
