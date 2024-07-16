package com.floordecor.inbound.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
    public static String convertToDate(String dateTimeStr) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, inputFormatter);

            return dateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
