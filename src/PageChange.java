import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageChange {
    private static URL url;

    static {
        try {
            url = new URL("https://trojmiasto.pl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static long interwal = 10L;
    private static long finish = 1_000_000L;
    private static List<String> emails = new ArrayList<>();
    private static boolean sound;
    private static final List<String> phrases = new ArrayList<>();
    private static DayOfWeek day;
    private static LocalTime hour;
    private static LocalDate date;
    private static final List<String> errorList = new ArrayList<>();
    private static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 1 && args[0].equals("--help")) {
            System.out.println(fullHelpText());
        } else {
            System.out.println(helpText());
        }
        argsParsing(args);
        printErrors();
        initialText();

        String oldPage = connection(url);
        while (finish > 0) {
            String tempPage = connection(url);
            if (emptyPageProtection(tempPage)) continue;
            check(tempPage, oldPage);
            finish--;
        }
    }

    private static void check(String tempPage, String oldPage) throws InterruptedException {
        if (!phrases.isEmpty()) {
            for (String phrase : phrases) {
                if (!tempPage.contains(phrase)) {
                    search(tempPage, phrase);
                } else {
                    printSuccess(" - ZNALEZIONO TEKST: " + phrase + "!!!!", phrase);
                }
            }
            System.out.println();
            Thread.sleep(interwal * 1_000);
        } else {
            if (tempPage.equals(oldPage)) {
                searchAndSleep(interwal * 1_000, tempPage);
            } else {
                printSuccess(" - JEST ZMIANA STRONY!!!!", null);
            }
        }
    }

    private static void printErrors() {
        if (!errorList.isEmpty()) {
            System.out.println();
            for (String error : errorList) {
                System.out.println(error);
            }
        }
    }

    private static String helpText() {
        return "Wpisz --help aby uzyskac pomoc.";
    }

    private static String fullHelpText() {
        return """
                Dostepne parametry:
                -u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)
                -i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)
                -f (finish) - ilosc iteracji programu
                -e (e-mail) - adres/y email na ktory chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila
                -s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru
                -date (date) - program bedzie sprawdzal czy jest juz po podanej dacie (dd-MM-yyyy), jak nie to zamknie program
                -d (day) - dzien tygodnia w ktorym zostanie uruchomiony skrypt (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
                -h (hour) - program bedzie sprawdzal czy jest juz po zadanej godzinie, i poczeka az bedzie po tej godzinie
                -p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila
                Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>""";
    }


    private static void argsParsing(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    try {
                        url = new URL(args[i + 1]);
                    } catch (MalformedURLException e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr URL, zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-i":
                    try {
                        interwal = Long.parseLong(args[i + 1]);
                    } catch (Exception e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr interwal, zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-f":
                    try {
                        finish = Long.parseLong(args[i + 1]);
                    } catch (Exception e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr finish, zostanie zignorowany! Uzyta zostanie wartosc domyslna.\u001B[0m");
                    }
                    break;
                case "-e":
                    for (int j = i + 1; j < args.length; j++) {
                        String email = args[j];
                        if (!email.equals("-u") && !email.equals("-i") && !email.equals("-f") && !email.equals("-e")
                                && !email.equals("-s") && !email.equals("-p") && !email.equals("-h")
                                && !email.equals("-d") && !email.equals("-date")) {
                            if (isValidEmail(args[j])) {
                                emails.add(email);
                            } else {
                                errorList.add("\u001B[31mNieprawidlowy parametr email, zostanie zignorowany!\u001B[0m");
                            }
                        } else {
                            break;
                        }
                    }

                    break;
                case "-s":
                    sound = true;
                    break;
                case "-p":
                    for (int j = i + 1; j < args.length; j++) {
                        String phrase = args[j].toLowerCase().trim().replaceAll("\\s", "");
                        if (!phrase.equals("-u") && !phrase.equals("-i") && !phrase.equals("-f") && !phrase.equals("-e")
                                && !phrase.equals("-s") && !phrase.equals("-p") && !phrase.equals("-h")
                                && !phrase.equals("-d") && !phrase.equals("-date")) {
                            phrases.add(phrase);
                        } else {
                            break;
                        }
                    }
                    break;
                case "-d":
                    try {
                        day = DayOfWeek.valueOf(args[i + 1]);
                    } catch (IllegalArgumentException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr day, zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-h":
                    try {
                        hour = LocalTime.of(Integer.parseInt(args[i + 1]), 0);
                    } catch (NumberFormatException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr hour, zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-date":
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    try {
                        date = LocalDate.parse(args[i + 1], formatter);
                    } catch (DateTimeParseException e) {
                        System.out.println("\u001B[31mNieprawidlowy parametr date, zostanie zignorowany!\u001B[0m");
                    }
                    break;
            }
        }
    }

    private static void checkHour() throws InterruptedException {
        LocalTime currentTime = LocalTime.now();
        if (!currentTime.isAfter(hour)) {
            long secondsDifference = Duration.between(currentTime, hour).abs().getSeconds();
            System.out.println("\u001B[31mNie jest po godzinie " + hour + ", usypiam program na " + secondsDifference / 60 + " minut.\u001B[0m");
            Thread.sleep(secondsDifference * 1000);
        }
    }

    private static void checkDay() throws InterruptedException {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (day == DayOfWeek.TUESDAY) {
            System.out.println("\u001B[31mDzis nie jest " + day + ", dzis jest " + dayOfWeek + "! Zamykam program.\u001B[0m");
            Thread.sleep(30_000);
            System.exit(0);
        }
    }

    private static void checkDate() throws InterruptedException {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(date)) {
            System.out.println("\u001B[31mDzis nie jest " + date + " lub pozniej, dzis jest dopiero " + currentDate + "! Zamykam program.\u001B[0m");
            Thread.sleep(30_000);
            System.exit(0);
        }
    }

    private static void initialText() throws InterruptedException {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedFinish = decimalFormat.format(finish);
        String initialTxt = "\nPARAMETRY PROGRAMU:";
        initialTxt = initialTxt.concat("\nStrona: \u001B[35m" + url + "\u001B[0m\n");
        initialTxt = initialTxt.concat("Czestotliwosc odswiezania: \u001B[35m" + interwal + "s \u001B[0m\n");
        initialTxt = initialTxt.concat("Koniec po: \u001B[35m" + formattedFinish + " iteracjach \u001B[0m\n");
        if (!emails.isEmpty()) {
            initialTxt = initialTxt.concat("Adres/y wysylki emaila: \u001B[35m");
            for (String email : emails) {
                initialTxt = initialTxt.concat(email + ", ");
            }
            initialTxt = initialTxt.substring(0, initialTxt.length() - 2).concat("\u001B[0m\n");
        }
        initialTxt = initialTxt.concat("Dzwiek: \u001B[35m" + sound + "\u001B[0m\n");
        if (date != null) initialTxt = initialTxt.concat("Po dacie: \u001B[35m" + date + "\u001B[0m\n");
        if (day != null) initialTxt = initialTxt.concat("Dzien tygodnia: \u001B[35m" + day + "\u001B[0m\n");
        if (hour != null) initialTxt = initialTxt.concat("Godzina: \u001B[35m" + hour + "\u001B[0m\n");
        if (!phrases.isEmpty()) {
            initialTxt = initialTxt.concat("Szukanie fraz:\n\u001B[35m");
            for (String phrase : phrases) {
                initialTxt = initialTxt.concat(phrase).concat("\n");
            }
            initialTxt = initialTxt.concat("\u001B[0m\n");
        }
        System.out.println(initialTxt);

        if (date != null) checkDate();
        if (day != null) checkDay();
        if (hour != null) checkHour();
    }

    private static String connection(URL url) throws Exception {
        String pageContent = null;
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
            Thread.sleep(5_000);
            System.exit(0);
        }
        return pageContent.toLowerCase().trim().replaceAll("\\s", "");
    }

    private static boolean emptyPageProtection(String tempPage) throws InterruptedException {
        if (tempPage.isEmpty()) {
            System.out.println("Pusta strona... Prawdopodobnie zly adres strony...");
            Thread.sleep(5_000);
            return true;
        }
        return false;
    }

    private static String getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static void playSound(int repeats) {
        for (int j = 0; j < repeats; j++) {
            try {
                InputStream inputStream = PageChange.class.getResourceAsStream("/resources/tada.wav");
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                Thread.sleep(clip.getMicrosecondLength() / 950);
                clip.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void search(String tempPage, String text) {
        System.out.println("\u001B[32m" + getTime() + " - szukam tekstu: " + text + "... Dlugosc strony: " + tempPage.length() + "\u001B[0m");
    }

    private static void searchAndSleep(Long millis, String tempPage) {
        try {
            System.out.println("\u001B[32m" + getTime() + " - sprawdzam czy podana strona sie zmienila... Dlugosc strony: " + tempPage.length() + "\u001B[0m");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printSuccess(String text, String phrase) throws InterruptedException {
        System.out.println("\u001B[01;41m" + getTime() + text + "\n\u001B[0m");
        if (!emails.isEmpty()) {
            for (String email : emails) {
                sendMail(email, url, phrase);
            }
        }
        if (sound) playSound(10_000);
        Thread.sleep(3_600_000);
        System.exit(0);
    }

    private static void sendMail(String email, URL urlString, String searchedPhrase) {
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
            txt = txt.concat("Strona: " + urlString + "\n");
            if (searchedPhrase != null) txt = txt.concat("Znaleziono text: " + searchedPhrase);
            message.setText(txt);

            Transport.send(message);
            System.out.println("Email na adres " + email + " zostal wyslany pomyslnie.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Wystąpil błąd podczas wysyłania emaila: " + e.getMessage());
        }
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = Pattern.compile(EMAIL_REGEX).matcher(email);
        return matcher.matches();
    }
}