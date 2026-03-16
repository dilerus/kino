package com.pagechange.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public String getCurrentTime() {
        return LocalDateTime.now().format(FORMATTER);
    }
}

