package com.freshworks.freddy.insights.helper;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class DateHelper {
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.systemDefault());

    /**
     * Converts the given {@link ZonedDateTime} to an ISO-8601 formatted string representation.
     * The resulting string represents the date and time in UTC timezone.
     *
     * @param dateTime the ZonedDateTime to convert to ISO-8601 format
     * @return the ISO-8601 formatted string representation of the given ZonedDateTime
     * @throws IllegalArgumentException if dateTime is null
     */
    public static String convertToISODate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime must not be null");
        }
        return ISO_DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * Formats the given {@link Date} object into a string representation using the provided date format pattern.
     *
     * @param date              the Date object to be formatted
     * @param dateFormatPattern the pattern for formatting the date, e.g., "yyyy-MM-dd'T'HH:mm:ssZ"
     * @return a string representation of the date in the specified format
     */
    public static String getFormattedDate(Date date, String dateFormatPattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
        return dateFormat.format(date);
    }
}
