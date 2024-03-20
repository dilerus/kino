import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Kino {
    private static String url = "https://trojmiasto.pl";
    private static Long interwal = 10L;
    private static Long finish = 1_000_000L;
    private static String email;
    private static Boolean sound = Boolean.FALSE;
    private static List<String> phrases;
    private static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static void main(String[] args) throws Exception {
        helpText();
        argsParsing(args);
        initialText();

        String oldPage = connection(url);
        while (finish > 0) {
            String tempPage = connection(url);
            if (emptyPageProtection(tempPage)) continue;

            if (phrases != null) {
                for (String phrase : phrases) {
                    if (!tempPage.contains(phrase)) {
                        search(tempPage, phrase);
                    } else {
                        printSuccess("  -  ZNALEZIONO TEKST: " + phrase + "!!!!", phrase);
                        break;
                    }
                }
                System.out.println();
                Thread.sleep(interwal * 1_000);
            } else {
                if (tempPage.equals(oldPage)) {
                    searchAndSleep(interwal * 1_000, tempPage);
                } else {
                    printSuccess("  -  JEST ZMIANA STRONY!!!!", null);
                    break;
                }
            }
            finish--;
        }
    }

    private static void helpText() {
        System.out.println("Parametry programu:");
        System.out.println("-u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)");
        System.out.println("-i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)");
        System.out.println("-f (finish) - ilosc iteracji programu");
        System.out.println("-e (e-mail) - adres email na ktory chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila");
        System.out.println("-s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru");
        System.out.println("-p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila");
        System.out.println("Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>");
    }


    private static void argsParsing(String[] args){
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-u")) {
                url = args[i + 1];
            }
            if (args[i].equals("-i")) {
                try {
                    interwal = Long.parseLong(args[i + 1]);
                } catch (Exception e) {
                    System.out.println("\n\u001B[31m-i Parametr interwal musi byc liczba! Ustawiam odswiezanie na domyslne 10s.\u001B[0m");
                    interwal = 10L;
                }
            }
            if (args[i].equals("-f")) {
                try {
                    finish = Long.parseLong(args[i + 1]);
                } catch (Exception e) {
                    System.out.println("\n\u001B[31m-f Parametr finish musi byc liczba! Uzyta zostanie wartosc domyslna.\u001B[0m");
                }
            }
            if (args[i].equals("-e")) {
                if (isValidEmail(args[i + 1])) {
                    email = args[i + 1];
                } else {
                    System.out.println("\n\u001B[31m-e Nieprawidlowy adres! Email nie zostanie wyslany.\u001B[0m");
                }
            }
            if (args[i].equals("-s")) {
                sound = Boolean.TRUE;
            }
            if (args[i].equals("-p")) {
                phrases = new ArrayList<>();
                for (int j = i + 1; j < args.length; j++) {
                    String phrase = args[j].toLowerCase().trim().replaceAll("\\s", "");
                    if (!phrase.equals("-u") && !phrase.equals("-i") && !phrase.equals("-f") && !phrase.equals("-e") && !phrase.equals("-s") && !phrase.equals("-p")) {
                        phrases.add(phrase);
                        i++;
                    } else {
                        break;
                    }
                }

            }
        }
    }

    private static void initialText() {
        String initialTxt = "\nPARAMETRY PROGRAMU:";
        initialTxt = initialTxt.concat("\nStrona: \u001B[35m" + url + "\u001B[0m\n");
        initialTxt = initialTxt.concat("Czestotliwosc odswiezania: \u001B[35m" + interwal + "s \u001B[0m\n");
        initialTxt = initialTxt.concat("Koniec po: \u001B[35m" + finish + " iteracjach \u001B[0m\n");
        if (email != null) initialTxt = initialTxt.concat("Adres wysylki emaila: \u001B[35m" + email + "\u001B[0m\n");
        initialTxt = initialTxt.concat("Dzwiek: \u001B[35m" + sound + "\u001B[0m\n");
        if (phrases != null) {
            initialTxt = initialTxt.concat("Szukanie fraz:\n\u001B[35m");
            for (String phrase : phrases) {
                initialTxt = initialTxt.concat(phrase).concat("\n");
            }
            initialTxt = initialTxt.concat("\u001B[0m\n");
        }
        System.out.println(initialTxt);
    }


    private static String connection(String urlString) throws Exception {
        String pageContent = null;
        try {
            URL url = new URL(urlString);
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
            Thread.sleep(5000);
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
                InputStream inputStream = Kino.class.getResourceAsStream("/resources/tada.wav");
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                Thread.sleep(clip.getMicrosecondLength() / 900);
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

    private static void printSuccess(String text, String phrase) {
        System.out.println("\u001B[31m" + getTime() + text);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + "\u001B[0m");
        if (email != null) sendMail(url, phrase);
        if (sound == Boolean.TRUE) playSound(10_000);
    }

    private static void sendMail(String urlString, String searchedPhrase) {
        playSound(3);
        // Dane do serwera pocztowego oraz logowania
        String host = "smtp.gmail.com"; // Tutaj podaj adres serwera SMTP
        String port = "587"; // Port serwera SMTP
        String username = "dilerus.robot"; // Adres e-mail z którego chcesz wysłać wiadomość
        String password = "qjbd zxst lotm ajbk"; // Hasło do konta e-mail

        // Ustawienia właściwości
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email)); // Adres odbiorcy
            message.setSubject("Sukces!");
            String txt = "Zmiana strony!!!\n";
            txt = txt.concat("Strona: " + urlString + "\n");
            if (searchedPhrase != null) txt = txt.concat("Znaleziono text: " + searchedPhrase);
            message.setText(txt);

            Transport.send(message);
            System.out.println("Email na adres " + email + " zostal wyslany pomyslnie.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas wysyłania emaila: " + e.getMessage());
        }
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = Pattern.compile(EMAIL_REGEX).matcher(email);
        return matcher.matches();
    }
}