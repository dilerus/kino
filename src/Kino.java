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
import java.util.Properties;


public class Kino {
    /*
        pierwszy argument - adres, np:   https://www.helios.pl/58,Gdansk/BazaFilmow/Szczegoly/film/30970/DIUNA%3A-Czesc-druga
        drugi argument - interwał odswiezania w sekundach
        trzeci argument - czy wysylac mail (true lub false)
        czwarty argument, opcjonalny, szukany tekst, np:   "<div class=""day-number"">19</div>"
        jesli go nie podasz bedzie sprawdzal zmiane strony
     */
    public static void main(String[] args) throws Exception {
        sprawdzanieArgsow(args);
        String urlString = args[0];

        Long interwal;
        try {
            interwal = Long.parseLong(args[1]);
        } catch (Exception e) {
            System.out.println("Drugi parametr musi byc liczba! Ustawiam odswiezanie na domyslne 10s");
            interwal = 10L;
        }

        Boolean emailSending = Boolean.parseBoolean(args[2]);

        initialText(args, urlString, interwal, emailSending);

        String oldPage = connection(urlString);
        Thread.sleep(interwal * 1000);
        int errorCounter = 0;
        while (true) {
            String tempPage = connection(urlString);
            if (tempPage.length() == 0) {
                errorCounter++;
                Thread.sleep(interwal * 1000);
                continue;
            }
            if (errorCounter >= 3) {
                System.out.println("Nie można połączyć się z podaną stroną.");
                break;
            }

            if (args.length >= 4) {
                for (int i = 3; i < args.length; i++) {
                    String searchedText = args[i].toLowerCase().trim().replaceAll("\\s", "");
                    if (!tempPage.toLowerCase().trim().replaceAll("\\s", "").contains(searchedText)) {
                        search(tempPage, searchedText);
                    } else {
                        printSuccess("  -  ZNALEZIONO TEKST: " + searchedText + "!!!!");
                        if (emailSending == Boolean.TRUE) sendMail(urlString, searchedText);
                        playSound(10000);
                        break;
                    }
                }
                System.out.println();
                Thread.sleep(interwal * 1000);
            } else {
                if (tempPage.equals(oldPage)) {
                    searchAndSleep(interwal * 1000, tempPage);
                } else {
                    printSuccess("  -  JEST ZMIANA STRONY!!!!");
                    if (emailSending == Boolean.TRUE) sendMail(urlString, null);
                    playSound(10000);
                    break;
                }
            }
        }
    }

    private static void initialText(String[] args, String urlString, Long interwal, Boolean emailSending) {
        String initialTxt = "\nStrona: \u001B[35m" + urlString + "\u001B[0m\n";
        initialTxt = initialTxt.concat("Czestotliwosc odswiezania: \u001B[35m" + interwal + "s \u001B[0m\n");
        initialTxt = initialTxt.concat("Wysylanie maila: \u001B[35m" + emailSending + "\u001B[0m\n");
        if (args.length >= 4) {
            initialTxt = initialTxt.concat("Szukanie fraz:\n\u001B[35m");
            for (int i = 3; i < args.length; i++) {
                initialTxt = initialTxt.concat(args[i].toLowerCase().trim().replaceAll("\\s", "").concat("\n"));
            }
            initialTxt = initialTxt.concat("\u001B[0m\n");
        }
        System.out.println(initialTxt);
    }

    private static void sprawdzanieArgsow(String[] args) throws InterruptedException {
        if (args.length < 3) {
            System.out.println("\nPotrzebne sa przynajmniej 3 parametry.");
            System.out.println("pierwszy argument - adres, np:   https://www.helios.pl/58,Gdansk/BazaFilmow/Szczegoly/film/30970/DIUNA%3A-Czesc-druga");
            System.out.println("drugi argument - interwał odswiezania w sekundach");
            System.out.println("trzeci argument - czy wysylac mail (true lub false)");
            System.out.println("czwarty argument, opcjonalny, szukany tekst, np: Diuna, jesli go nie podasz, program bedzie sprawdzal czy zaszla zmiana strony");
            Thread.sleep(5000);
            System.exit(0);
        }
    }

    private static String connection(String urlString) throws Exception {
        String pageContent = new String();
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
        return pageContent;
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

    private static void printSuccess(String text) {
        System.out.println("\u001B[31m" + getTime() + text);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + "\u001B[0m");
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
            // Tworzenie obiektu MimeMessage
            Message message = new MimeMessage(session);
            // Ustawienie nadawcy
            message.setFrom(new InternetAddress(username));
            // Ustawienie odbiorcy
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("dilerus@gmail.com")); // Adres odbiorcy
            // Ustawienie tematu
            message.setSubject("Sukces!");
            // Ustawienie treści wiadomości
            String txt = new String();
            txt = txt.concat("Zmiana strony!!!\n");
            txt = txt.concat("Strona: " + urlString + "\n");
            if (searchedPhrase != null) txt = txt.concat("Znaleziono text: " + searchedPhrase);
            message.setText(txt);

            // Wysłanie wiadomości
            Transport.send(message);
            System.out.println("Email zostal wyslany pomyslnie.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Wystąpił błąd podczas wysyłania emaila: " + e.getMessage());
        }
    }
}