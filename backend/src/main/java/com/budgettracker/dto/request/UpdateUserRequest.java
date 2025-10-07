package com.budgettracker.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String timezone;
    private String locale;
    private String currencyCode;
}
