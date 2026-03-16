package com.pagechange.util;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.config.MonitoringMode;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class ConsoleOutputFormatter {
    private final AnsiColorFormatter colorFormatter;
    private final TimeFormatter timeFormatter;

    public ConsoleOutputFormatter(AnsiColorFormatter colorFormatter, TimeFormatter timeFormatter) {
        this.colorFormatter = colorFormatter;
        this.timeFormatter = timeFormatter;
    }

    public void printInitialConfiguration(MonitoringConfig config) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedFinish = decimalFormat.format(config.getFinish());

        StringBuilder txt = new StringBuilder("\nPARAMETRY PROGRAMU:\n");
        txt.append("Strona: ").append(colorFormatter.highlight(config.getUrl().toString())).append("\n");
        txt.append("Tryb: ").append(colorFormatter.highlight(config.getMode().getLabel())).append("\n");
        txt.append("Czestotliwosc odswiezania: ").append(colorFormatter.highlight(config.getInterval() + "s")).append("\n");
        txt.append("Koniec po: ").append(colorFormatter.highlight(formattedFinish + " iteracjach")).append("\n");

        if (!config.getEmails().isEmpty()) {
            txt.append("Adres/y wysylki emaila: ").append(colorFormatter.highlight(String.join(", ", config.getEmails()))).append("\n");
        }

        txt.append("Dzwiek: ").append(colorFormatter.highlight(String.valueOf(config.isSound()))).append("\n");

        if (config.getDate() != null) {
            txt.append("Po dacie: ").append(colorFormatter.highlight(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy").format(config.getDate()))).append("\n");
        }

        if (config.getDay() != null) {
            txt.append("Dzien tygodnia: ").append(colorFormatter.highlight(config.getDay().toString())).append("\n");
        }

        if (config.getHour() != null) {
            txt.append("Godzina: ").append(colorFormatter.highlight(config.getHour().toString())).append("\n");
        }

        if (config.isNegation()) {
            txt.append("Negacja: ").append(colorFormatter.highlight("true")).append("\n");
        }

        appendModeSpecificInfo(txt, config);

        System.out.println(txt.toString());
    }

    private void appendModeSpecificInfo(StringBuilder txt, MonitoringConfig config) {
        MonitoringMode mode = config.getMode();

        switch (mode) {
            case PHRASES:
                if (!config.getPhrases().isEmpty()) {
                    txt.append("Szukane frazy:\n");
                    for (String phrase : config.getPhrases()) {
                        txt.append(colorFormatter.highlight(phrase)).append("\n");
                    }
                }
                break;
            case VALUE_BIGGER:
                txt.append("Szukanie wartosci wiekszej niz: ")
                        .append(colorFormatter.highlight(config.getThresholdValue().toString())).append("\n");
                break;
            case VALUE_SMALLER:
                txt.append("Szukanie wartosci mniejszej niz: ")
                        .append(colorFormatter.highlight(config.getThresholdValue().toString())).append("\n");
                break;
            case SITE_BIGGER_THAN:
                txt.append("Strona ma byc wieksza niz: ")
                        .append(colorFormatter.highlight(String.valueOf(config.getSiteSize()))).append("\n");
                break;
            case SITE_SMALLER_THAN:
                txt.append("Strona ma byc mniejsza niz: ")
                        .append(colorFormatter.highlight(String.valueOf(config.getSiteSize()))).append("\n");
                break;
        }
    }

    public void printSuccess(String message) {
        System.out.println(colorFormatter.formatSuccess(timeFormatter.getCurrentTime() + " - SUKCES - " + message) + "\n");
    }

    public void printDefeat(String message) {
        System.out.println(colorFormatter.formatInfo(timeFormatter.getCurrentTime() + " - " + message));
    }

    public void printEmptyPageWarning(int attemptNumber, int maxAttempts) {
        System.out.println(timeFormatter.getCurrentTime() + " - " + attemptNumber +
                " proba - Pusta strona... Prawdopodobnie chwilowy brak internetu lub blad serwera.");
    }

    public void printInitialEmptyPageWarning(int attemptNumber, int maxAttempts) {
        System.out.print("Pusta strona... Prawdopodobnie zly adres lub brak internetu...");
        System.out.println(" Ponawiam probe za 30s. (" + attemptNumber + "/" + maxAttempts + ")");
    }

    public void printIncrementedPhrase(String phrase) {
        if (phrase != null) {
            System.out.println(colorFormatter.highlight(phrase) + "\n");
        }
    }
}

