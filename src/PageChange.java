import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageChange {
    private static final List<String> EMAILS = new ArrayList<>();
    private static final List<String> PHRASES = new ArrayList<>();
    private static final List<String> AVAILABLE_PARAMETERS = Arrays.asList("-u", "-i", "-f", "-e", "-s", "-p", "-h", "-d", "-date", "-n", "-vb", "-vs");
    private static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static URL url;
    private static long interval = 10L;
    private static long finish = 1_000_000L;
    private static boolean sound;
    private static boolean negation;
    private static DayOfWeek day;
    private static LocalTime hour;
    private static LocalDate date;
    private static String preValue;
    private static Float thresholdValue;
    private static Float actualValue;
    private static boolean checkValue;
    private static boolean isBigger;

    static {
        try {
            url = new URI("https://trojmiasto.pl").toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        helpText(args);
        argsParsing(args);
        initialText();

        String oldPage = null;
        for (int i = 1; i <= 4; i++) {
            oldPage = connection(url);
            if (!oldPage.isEmpty()) break;
            initialEmptyPageProtection(i);
        }
        int emptyPageIndicator = 1;
        while (finish > 0) {
            String tempPage = connection(url);
            if (emptyPageProtection(emptyPageIndicator, tempPage)) {
                emptyPageIndicator++;
                sleep(interval * 1_000);
                continue;
            }
            check(tempPage, oldPage);
            finish--;
            emptyPageIndicator = 1;
        }
        System.out.println("Wartosc parametru 'finish' doszla do 0.");
        exit(30);
    }

    public static void helpText(String[] args) {
        if (args != null && args.length == 1 && args[0].equals("--help")) {
            System.out.println(fullHelpText());
        } else {
            System.out.println(shortHelpText());
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.getCause();
        }
    }

    private static void check(String tempPage, String oldPage) {
        if (!PHRASES.isEmpty()) {
            for (String phrase : PHRASES) {
                if ((!negation && !tempPage.contains(phrase)) || (negation && tempPage.contains(phrase))) {
                    print(tempPage, phrase);
                } else {
                    printSuccess(phrase);
                }
            }
            System.out.println();
            sleep(interval * 1_000);
        } else {
            if (checkValue) {
                setActualValue(tempPage);
                if ((checkActualValueAgainstThresholdValue() && isBigger) || (!checkActualValueAgainstThresholdValue() && !isBigger)) {
                    printSuccess(null);
                } else {
                    printDefeatAndSleep(tempPage);
                }
            } else {
                if (tempPage.equals(oldPage)) {
                    printDefeatAndSleep(tempPage);
                } else {
                    printSuccess(null);
                }
            }
        }
    }

    private static String shortHelpText() {
        return "Wpisz --help aby uzyskac pomoc.";
    }

    private static String fullHelpText() {
        return """
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
                -n (negacja) - gdy ustawiona, program bedzie czekal az podane frazy znikna ze strony, nie potrzebuje dodatkowego parametru
                -vb (value bigger) - gdy ustawiona, pierewszy parametr jest fragmentem strony przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej bedzie sukces
                -vs (value smaller) - gdy ustawiona, pierewszy parametr jest fragmentem strony przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej bedzie sukces
                Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>""";
    }

    private static void argsParsing(String[] args) {
        if (args == null) return;
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    try {
                        url = new URI(args[i + 1]).toURL();
                    } catch (RuntimeException | URISyntaxException | MalformedURLException e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr URL (" + args[i + 1] + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-i":
                    try {
                        interval = Long.parseLong(args[i + 1]);
                    } catch (Exception e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr interwal (" + args[i + 1] + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-f":
                    try {
                        finish = Long.parseLong(args[i + 1]);
                    } catch (Exception e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr finish (" + args[i + 1] + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna.\u001B[0m");
                    }
                    break;
                case "-e":
                    for (int j = i + 1; j < args.length; j++) {
                        String email = args[j];
                        if (!AVAILABLE_PARAMETERS.contains(email)) {
                            if (isValidEmail(email)) {
                                EMAILS.add(email);
                            } else {
                                errorList.add("\u001B[31mNieprawidlowy parametr email (" + email + "), zostanie zignorowany!\u001B[0m");
                            }
                        } else {
                            break;
                        }
                    }
                    break;
                case "-s":
                    sound = true;
                    break;
                case "-n":
                    negation = true;
                    break;
                case "-p":
                    if (checkValue) {
                        System.out.println("\u001B[31mParametr 'value bigger' lub 'value smaller' zostanie zignorowany, bedzie sprawdzany parametr 'phrases'!\u001B[0m");
                    }
                    for (int j = i + 1; j < args.length; j++) {
                        String phrase = removePolishChars(args[j].toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", ""));
                        if (!AVAILABLE_PARAMETERS.contains(phrase)) {
                            PHRASES.add(phrase);
                        } else {
                            break;
                        }
                    }
                    break;
                case "-d":
                    try {
                        day = DayOfWeek.valueOf(args[i + 1]);
                    } catch (IllegalArgumentException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr 'day' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-h":
                    try {
                        hour = LocalTime.of(Integer.parseInt(args[i + 1]), 0);
                    } catch (NumberFormatException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr 'hour' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-date":
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    try {
                        date = LocalDate.parse(args[i + 1], formatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("\u001B[31mNieprawidlowy parametr 'date' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-vb":
                    if (!PHRASES.isEmpty()) {
                        System.out.println("\u001B[31mParametr 'value bigger' (" + args[i + 1] + ") zostanie zignorowany, bedzie sprawdzany parametr 'phrases'!\u001B[0m");
                        break;
                    }
                    preValue = removePolishChars(args[i + 1].toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", ""));
                    thresholdValue = Float.parseFloat(args[i + 2].replaceAll(",", "."));
                    checkValue = true;
                    isBigger = true;
                    break;
                case "-vs":
                    if (!PHRASES.isEmpty()) {
                        System.out.println("\u001B[31mParametr 'value smaller' (" + args[i + 1] + ") zostanie zignorowany, bedzie sprawdzany parametr 'phrases'!\u001B[0m");
                        break;
                    }
                    preValue = removePolishChars(args[i + 1].toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", ""));
                    thresholdValue = Float.parseFloat(args[i + 2].replaceAll(",", "."));
                    checkValue = true;
                    isBigger = false;
                    break;
            }
        }
        if (!errorList.isEmpty()) {
            System.out.println();
            for (String error : errorList) {
                System.out.println(error);
            }
        }
    }

    private static String removePolishChars(String input) {
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalizedString).replaceAll("");
    }

    private static void checkHour() {
        LocalTime currentTime = LocalTime.now();
        if (!currentTime.isAfter(hour)) {
            long secondsDifference = Duration.between(currentTime, hour).abs().getSeconds();
            System.out.println("\u001B[31mNie jest po godzinie " + hour + ", usypiam program na " + secondsDifference / 60 + " minut.\u001B[0m");
            sleep(secondsDifference * 1_000);
        }
    }

    private static void checkDay() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (day != dayOfWeek) {
            System.out.println("\u001B[31mDzis nie jest " + day + ", dzis jest " + dayOfWeek + "!\u001B[0m");
            exit(30);
        }
    }

    public static void exit(long sec) {
        System.out.println("Czekam " + sec + " sekund i zamykam program.");
        sleep(sec * 1_000);
        System.exit(0);
    }

    private static void checkDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formattedCurrentDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (currentDate.isBefore(date)) {
            System.out.println("\u001B[31mDzis nie jest " + formattedCurrentDate.format(date) + " lub pozniej, dzis jest dopiero " + formattedCurrentDate.format(currentDate) + "!\u001B[0m");
            exit(30);
        }
    }

    private static void initialText() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedFinish = decimalFormat.format(finish);
        String initialTxt = "\nPARAMETRY PROGRAMU:";
        initialTxt += "\nStrona: \u001B[35m" + url + "\u001B[0m\n";
        initialTxt += "Czestotliwosc odswiezania: \u001B[35m" + interval + "s \u001B[0m\n";
        initialTxt += "Koniec po: \u001B[35m" + formattedFinish + " iteracjach \u001B[0m\n";
        if (!EMAILS.isEmpty()) {
            initialTxt += "Adres/y wysylki emaila: \u001B[35m";
            for (String email : EMAILS) {
                initialTxt = initialTxt.concat(email + ", ");
            }
            initialTxt = initialTxt.substring(0, initialTxt.length() - 2).concat("\u001B[0m\n");
        }
        initialTxt += "Dzwiek: \u001B[35m" + sound + "\u001B[0m\n";
        if (date != null)
            initialTxt += "Po dacie: \u001B[35m" + DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date) + "\u001B[0m\n";
        if (day != null) initialTxt += "Dzien tygodnia: \u001B[35m" + day + "\u001B[0m\n";
        if (hour != null) initialTxt += "Godzina: \u001B[35m" + hour + "\u001B[0m\n";
        if (negation) initialTxt += "Negacja: \u001B[35m" + negation + "\u001B[0m\n";
        if (!PHRASES.isEmpty()) {
            initialTxt += "Szukane frazy:\n\u001B[35m";
            for (String phrase : PHRASES) {
                initialTxt = initialTxt.concat(phrase).concat("\n");
            }
            initialTxt += "\u001B[0m\n";
        }
        if (checkValue && PHRASES.isEmpty()) {
            initialTxt += "Szukanie wartosci";
            initialTxt += (isBigger) ? " wiekszej " : " mniejszej ";
            initialTxt += "niz: \u001B[35m" + thresholdValue + "\u001B[0m\n";
        }
        System.out.println(initialTxt);
        if (date != null) checkDate();
        if (day != null) checkDay();
        if (hour != null) checkHour();
    }

    private static String connection(URL url) {
        String pageContent;
        try {
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
            reader.close();
            pageContent = content.toString();
        } catch (IOException e) {
            System.out.println("Nie udalo sie polaczyc z podanym adresem!");
            return "";
        }
        return removePolishChars(pageContent.toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", ""));
    }

    private static void initialEmptyPageProtection(int i) {
        System.out.print("Pusta strona... Prawdopodobnie zly adres lub brak internetu...");
        if (i > 3) {
            exit(30);
        }
        System.out.println(" Ponawiam probe za 30s. (" + i + "/3)");
        sleep(30_000);
    }

    private static boolean emptyPageProtection(int number, String tempPage) {
        if (tempPage.isEmpty()) {
            System.out.println(getTime() + " - " + number + " proba - Pusta strona... Prawdopodobnie chwilowy brak internetu lub blad serwera.");
            return true;
        }
        return false;
    }

    private static String getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return now.format(formatter);
    }

    private static boolean checkActualValueAgainstThresholdValue() {
        return actualValue.compareTo(thresholdValue) > 0;
    }


    private static void setActualValue(String page) {
        int position = page.indexOf(preValue);
        if (position == -1) {
            System.out.println("Fragment '" + preValue + "' nie został znaleziony na stronie.");
            exit(30);
        }
        float number = 0;
        String text = page.substring(position + preValue.length(), position + preValue.length() + 20);
        Pattern pattern = Pattern.compile("[0-9,]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String numberStr = matcher.group().replaceAll(",", ".");
            number = Float.parseFloat(numberStr);
        }
        actualValue = number;
    }

    private static void playSound(int repeats) {
        for (int j = 0; j < repeats; j++) {
            try {
                InputStream inputStream = PageChange.class.getResourceAsStream("/resources/tada.wav");
                if (inputStream != null) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                    Thread.sleep(clip.getMicrosecondLength() / 950);
                    clip.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void printDefeatAndSleep(String tempPage) {
        String result = "\u001B[32m" + getTime() + " - ";
        if (checkValue) {
            result += "znaleziona wartosc: '" + actualValue + "' nie jest ";
            result += isBigger ? "wieksza" : "mniejsza";
            result += " niz ustawiona wartosc progowa: '" + thresholdValue + "'.";
        } else {
            result += "sprawdzam czy podana strona sie zmienila... Dlugosc strony: " + tempPage.length();
        }
        System.out.println(result + "\u001B[0m");
        sleep(interval * 1_000);
    }

    private static void print(String tempPage, String phrase) {
        String result = "\u001B[32m" + getTime() + " - szukam ";
        if (negation) result += "braku ";
        result += "tekstu: " + phrase + "... ";
        System.out.println(result + "Dlugosc strony: " + tempPage.length() + "\u001B[0m");
    }

    private static void printSuccess(String phrase) {
        String result = "\u001B[01;41m" + getTime() + " - SUKCES - ";
        if (!PHRASES.isEmpty()) {
            if (negation) result += "nie znaleziono frazy: ";
            else result += "znaleziono fraze: ";
            result += phrase;
        } else {
            if (checkValue) {
                result += "Znaleziona wartosc: " + actualValue + " jest ";
                if (checkActualValueAgainstThresholdValue()) result += "wieksza";
                else result += "mniejsza";
                result += " niz ustawiona wartosc progowa: " + thresholdValue;
            } else result += "jest zmiana strony";
        }
        System.out.println(result + "\n\u001B[0m");
        if (!EMAILS.isEmpty()) {
            for (String email : EMAILS) {
                sendMail(email, url, phrase, 1, 5);
            }
        }
        if (sound) playSound(10_000);
        exit(3_600);
    }

    private static void sendMail(String email, URL urlString, String searchedPhrase, int retries, int retriesLimit) {
        if (sound) playSound(3);
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "dilerus.robot";
        String password = "qjbd zxst lotm ajbk";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Sukces!");
            String txt = "Zmiana strony!!!\n";
            txt += "Strona: " + urlString + "\n";
            if (searchedPhrase != null) {
                if (negation) txt += "Nie znaleziono textu: " + searchedPhrase;
                else txt += "Znaleziono text: " + searchedPhrase;
            }
            message.setText(txt);
            Transport.send(message);
            System.out.println("Email na adres " + email + " zostal wyslany pomyslnie.");

        } catch (Exception e) {
            if (retries <= retriesLimit) {
                System.out.println("Wystąpil bląd podczas wysylania emaila" + e.getMessage() + ", ponawiam probe wyslania maila, proba nr " + retries + "/" + retriesLimit);
                sleep(30_000);
                sendMail(email, urlString, searchedPhrase, ++retries, retriesLimit);
            }
        }
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = Pattern.compile(EMAIL_REGEX).matcher(email);
        return matcher.matches();
    }
}