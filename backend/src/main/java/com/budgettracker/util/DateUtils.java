package com.budgettracker.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DateUtils {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static LocalDate getStartOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate getEndOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    public static LocalDate getStartOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    public static LocalDate getEndOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    public static LocalDateTime toZonedDateTime(LocalDateTime dateTime, String timezone) {
        return ZonedDateTime.of(dateTime, ZoneId.of(timezone)).toLocalDateTime();
    }

    public static String formatDate(LocalDate date) {
        return date.format(ISO_DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(ISO_DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, ISO_DATE_FORMATTER);
    }

    public static boolean isInDateRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }
}
