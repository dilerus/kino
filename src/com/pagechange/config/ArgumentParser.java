package com.pagechange.config;

import com.pagechange.util.NumericValueExtractor;
import com.pagechange.util.StringNormalizer;
import com.pagechange.validation.EmailValidator;
import com.pagechange.validation.ValidationError;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentParser {
    private static final List<String> AVAILABLE_PARAMETERS = Arrays.asList(
            "-mode", "-ss", "-u", "-i", "-f", "-e", "-s", "-p", "-h", "-d",
            "-date", "-n", "-vb", "-vs", "-inc", "-debug", "-bt", "-st", "--help");

    private final EmailValidator emailValidator;
    private final StringNormalizer stringNormalizer;
    private final NumericValueExtractor numericExtractor;

    public ArgumentParser(EmailValidator emailValidator,
                         StringNormalizer stringNormalizer,
                         NumericValueExtractor numericExtractor) {
        this.emailValidator = emailValidator;
        this.stringNormalizer = stringNormalizer;
        this.numericExtractor = numericExtractor;
    }

    public MonitoringConfig parse(String[] args) {
        if (args == null || args.length == 0) {
            return MonitoringConfig.builder().build();
        }

        if (args.length == 1 && args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }

        MonitoringConfig.Builder builder = MonitoringConfig.builder();
        List<ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    if (i + 1 < args.length) {
                        parseUrl(args[i + 1], builder, errors);
                    }
                    break;
                case "-i":
                    if (i + 1 < args.length) {
                        parseInterval(args[i + 1], builder, errors);
                    }
                    break;
                case "-f":
                    if (i + 1 < args.length) {
                        parseFinish(args[i + 1], builder, errors);
                    }
                    break;
                case "-e":
                    i = parseEmails(args, i, builder, errors);
                    break;
                case "-s":
                    builder.sound(true);
                    break;
                case "-n":
                    builder.negation(true);
                    break;
                case "-p":
                    i = parsePhrases(args, i, builder, errors);
                    break;
                case "-d":
                    if (i + 1 < args.length) {
                        parseDay(args[i + 1], builder, errors);
                    }
                    break;
                case "-h":
                    if (i + 1 < args.length) {
                        parseHour(args[i + 1], builder, errors);
                    }
                    break;
                case "-date":
                    if (i + 1 < args.length) {
                        parseDate(args[i + 1], builder, errors);
                    }
                    break;
                case "-vb":
                    if (i + 2 < args.length) {
                        parseValueBigger(args[i + 1], args[i + 2], builder, errors);
                    }
                    break;
                case "-vs":
                    if (i + 2 < args.length) {
                        parseValueSmaller(args[i + 1], args[i + 2], builder, errors);
                    }
                    break;
                case "-inc":
                    if (i + 1 < args.length) {
                        parseIncrement(args[i + 1], builder, errors);
                    }
                    break;
                case "-bt":
                    if (i + 1 < args.length) {
                        parseBiggerThan(args[i + 1], builder, errors);
                    }
                    break;
                case "-st":
                    if (i + 1 < args.length) {
                        parseSmallerThan(args[i + 1], builder, errors);
                    }
                    break;
                case "-debug":
                    builder.debug(true);
                    break;
            }
        }

        handleErrors(errors);
        return builder.build();
    }

    private void parseUrl(String urlString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            URL url = new URI(urlString).toURL();
            builder.url(url);
        } catch (URISyntaxException | MalformedURLException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr URL (" + urlString + ")\u001B[0m", true));
        }
    }

    private void parseInterval(String intervalString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            long interval = Long.parseLong(intervalString);
            builder.interval(interval);
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr interwal (" + intervalString + ")\u001B[0m", true));
        }
    }

    private void parseFinish(String finishString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            long finish = Long.parseLong(finishString);
            builder.finish(finish);
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr finish (" + finishString + ")\u001B[0m", true));
        }
    }

    private int parseEmails(String[] args, int startIndex, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        int i = startIndex;
        for (int j = i + 1; j < args.length; j++) {
            String email = args[j];
            if (!AVAILABLE_PARAMETERS.contains(email)) {
                if (emailValidator.isValid(email)) {
                    builder.addEmail(email);
                    i = j;
                } else {
                    errors.add(new ValidationError(
                            "\u001B[31mNieprawidlowy parametr email (" + email + ")\u001B[0m", true));
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int parsePhrases(String[] args, int startIndex, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        if (builder.build().getMode() == null || builder.build().getMode() == MonitoringMode.CHECK_VALUE) {
            builder.mode(MonitoringMode.PHRASES);
        }

        int i = startIndex;
        for (int j = i + 1; j < args.length; j++) {
            String phrase = args[j];
            if (!AVAILABLE_PARAMETERS.contains(phrase)) {
                builder.addPhrase(stringNormalizer.normalize(phrase));
                i = j;
            } else {
                break;
            }
        }
        return i;
    }

    private void parseDay(String dayString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            DayOfWeek day = DayOfWeek.valueOf(dayString);
            builder.day(day);
        } catch (IllegalArgumentException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr 'day' (" + dayString + ")\u001B[0m", true));
        }
    }

    private void parseHour(String hourString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            int hour = Integer.parseInt(hourString);
            builder.hour(LocalTime.of(hour, 0));
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr 'hour' (" + hourString + ")\u001B[0m", true));
        }
    }

    private void parseDate(String dateString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            LocalDate date = LocalDate.parse(dateString, formatter);
            builder.date(date);
        } catch (DateTimeParseException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr 'date' (" + dateString + ")\u001B[0m", true));
        }
    }

    private void parseValueBigger(String prefix, String threshold, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            builder.mode(MonitoringMode.VALUE_BIGGER);
            builder.preValue(stringNormalizer.normalize(prefix));
            builder.thresholdValue(Float.parseFloat(threshold.replaceAll(",", ".")));
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr threshold value (" + threshold + ")\u001B[0m", true));
        }
    }

    private void parseValueSmaller(String prefix, String threshold, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            builder.mode(MonitoringMode.VALUE_SMALLER);
            builder.preValue(stringNormalizer.normalize(prefix));
            builder.thresholdValue(Float.parseFloat(threshold.replaceAll(",", ".")));
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr threshold value (" + threshold + ")\u001B[0m", true));
        }
    }

    private void parseIncrement(String prefix, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        builder.mode(MonitoringMode.PHRASES);
        builder.prefixIncrementation(stringNormalizer.normalize(prefix));
    }

    private void parseBiggerThan(String sizeString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            long size = Long.parseLong(sizeString);
            builder.mode(MonitoringMode.SITE_BIGGER_THAN);
            builder.siteSize(size);
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr siteSize (" + sizeString + ")\u001B[0m", true));
        }
    }

    private void parseSmallerThan(String sizeString, MonitoringConfig.Builder builder, List<ValidationError> errors) {
        try {
            long size = Long.parseLong(sizeString);
            builder.mode(MonitoringMode.SITE_SMALLER_THAN);
            builder.siteSize(size);
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(
                    "\u001B[31mNieprawidlowy parametr siteSize (" + sizeString + ")\u001B[0m", true));
        }
    }

    private void handleErrors(List<ValidationError> errors) {
        if (!errors.isEmpty()) {
            System.out.println("Errory:");
            errors.stream()
                    .filter(error -> !error.isFatal())
                    .forEach(error -> System.out.println(error.getMessage()));

            System.out.println("\nErrory powazne:");
            errors.stream()
                    .filter(ValidationError::isFatal)
                    .forEach(error -> System.out.println(error.getMessage()));

            if (errors.stream().anyMatch(ValidationError::isFatal)) {
                System.out.println("\nWystapily bledy podczas parsowania argumentow, " +
                        "poniewaz niektore z nich sa krytyczne, program zostanie zamkniety.");
                System.exit(1);
            }
        }
    }

    private void printHelp() {
        String helpText = """
                Wpisz --help aby uzyskac pomoc.
                
                Dostepne parametry:
                -u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)
                -i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)
                -f (finish) - ilosc iteracji programu (domyslnie: 1.000.000)
                -e (e-mail) - adres/y email na ktora chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila
                -s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru
                -date (date) - program bedzie sprawdzal czy jest juz po podanej dacie (dd-MM-yyyy), jak nie, to zamknie program
                -d (day) - dzien tygodnia w ktorym zostanie uruchomiony skrypt (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
                -h (hour) - program bedzie sprawdzal czy jest juz po zadanej godzinie, i poczeka az bedzie po tej godzinie
                -p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila
                -n (negate) - gdy ustawiona, program bedzie czekal az podane frazy znikna ze strony, nie potrzebuje dodatkowego parametru
                -vb (value bigger) - dwa parametry, pierewszy parametr to prefix przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej w gore bedzie sukces
                -vs (value smaller) - dwa parametry, pierewszy parametr to prefix przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej w dol bedzie sukces
                -inc (increment) - prefix przed wartoscia liczbowa, gdy ustawione, program laduje do szukanych fraz fraze zlozona z prefixu i wartosci zwiekszonej o jeden
                -bt (bigger than) - jesli wartosc strony jest wieksza niz deklarowana wartosc, to sukces
                -st (smaller than) - jesli wartosc strony jest mniejsza niz deklarowana wartosc, to sukces
                -debug - jesli flaga obecna, program wyprintuje zawartosc strony
                Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>""";
        System.out.println(helpText);
    }
}

