package com.pagechange.validation;

import com.pagechange.util.AnsiColorFormatter;
import com.pagechange.util.ExitHandler;
import com.pagechange.util.Sleeper;
import com.pagechange.config.AppConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeValidator {
    private final AnsiColorFormatter formatter;
    private final Sleeper sleeper;
    private final ExitHandler exitHandler;

    public TimeValidator(AnsiColorFormatter formatter, Sleeper sleeper, ExitHandler exitHandler) {
        this.formatter = formatter;
        this.sleeper = sleeper;
        this.exitHandler = exitHandler;
    }

    public void validateDate(LocalDate requiredDate) {
        if (requiredDate == null) {
            return;
        }

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        if (currentDate.isBefore(requiredDate)) {
            System.out.println(formatter.formatError(
                "Dzis nie jest " + dateFormatter.format(requiredDate) +
                " lub pozniej, dzis jest dopiero " + dateFormatter.format(currentDate) + "!"
            ));
            exit(AppConstants.EXIT_DELAY_SECONDS);
        }
    }

    public void validateDay(DayOfWeek requiredDay) {
        if (requiredDay == null) {
            return;
        }

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        if (requiredDay != currentDay) {
            System.out.println(formatter.formatError(
                "Dzis nie jest " + requiredDay + ", dzis jest " + currentDay + "!"
            ));
            exit(AppConstants.EXIT_DELAY_SECONDS);
        }
    }

    public void validateHour(LocalTime requiredHour) {
        if (requiredHour == null) {
            return;
        }

        LocalTime currentTime = LocalTime.now();
        if (!currentTime.isAfter(requiredHour)) {
            long secondsDifference = Duration.between(currentTime, requiredHour).abs().getSeconds();
            System.out.println(formatter.formatError(
                "Nie jest po godzinie " + requiredHour +
                ", usypiam program na " + secondsDifference / 60 + " minut."
            ));
            sleeper.sleep(secondsDifference * 1_000);
        }
    }

    private void exit(long seconds) {
        System.out.println("Czekam " + seconds + " sekund i zamykam program.");
        sleeper.sleep(seconds * 1_000);
        exitHandler.exit(0);
    }
}

