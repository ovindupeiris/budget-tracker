package com.budgettracker.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils {

    public static BigDecimal convert(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (IllegalArgumentException e) {
            return amount.setScale(2, RoundingMode.HALF_UP).toString() + " " + currencyCode;
        }
    }

    public static String getSymbol(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode).getSymbol();
        } catch (IllegalArgumentException e) {
            return currencyCode;
        }
    }

    public static boolean isValidCurrencyCode(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static BigDecimal roundToTwoDecimals(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
