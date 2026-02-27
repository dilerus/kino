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
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageChange {
    private final Website website;

    public PageChange() {
        this.website = new Website();
    }

    public static void main(String[] args) {
        PageChange pageChange = new PageChange();

        pageChange.helpText(args);
        pageChange.argsParsing(args);
        pageChange.initialText();

        String oldPage = null;
        for (int i = 1; i <= 4; i++) {
            oldPage = pageChange.connection(pageChange.website.getUrl());
            if (!oldPage.isEmpty()) {
                break;
            }
            pageChange.initialEmptyPageProtection(i);
        }
        if (pageChange.website.getMode() == Util.Mode.PHRASES && pageChange.website.getPrefixIncrementation() != null) {
            pageChange.loadIncrementationPhrase(oldPage);
        }
        int emptyPageIndicator = 1;
        while (pageChange.website.getFinish() > 0) {
            pageChange.website.setTempPage(pageChange.connection(pageChange.website.getUrl()));
            if (pageChange.emptyPageProtection(emptyPageIndicator, pageChange.website.getTempPage())) {
                emptyPageIndicator++;
                pageChange.sleep(pageChange.website.getInterval() * 1_000);
                continue;
            }
            if (pageChange.website.isDebug()) {
                System.out.println(pageChange.website.getTempPage());
            }
            pageChange.check(pageChange.website.getTempPage(), oldPage);
            pageChange.website.setFinish(pageChange.website.getFinish() - 1);
            emptyPageIndicator = 1;
        }
        System.out.println("Wartosc parametru 'finish' doszla do 0.");
        pageChange.exit(30);
    }

    private void loadIncrementationPhrase(String oldPage) {
        if (oldPage.contains(this.website.getPrefixIncrementation())) {
            int startPos = oldPage.indexOf(this.website.getPrefixIncrementation()) + this.website.getPrefixIncrementation().length();
            int endPos = Math.min(startPos + 20, oldPage.length());
            String textAfterPrefix = oldPage.substring(startPos, endPos);

            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(textAfterPrefix);

            if (matcher.find()) {
                long incrementationValue = Long.parseLong(matcher.group());
                incrementationValue++;
                this.website.getPHRASES().add(this.website.getPrefixIncrementation() + incrementationValue);
                if (this.website.getPHRASES().size() == 1) {
                    System.out.println("Szukane frazy:");
                }
                System.out.println("\u001B[35m" + this.website.getPrefixIncrementation() + incrementationValue + "\u001B[0m\n");
            } else {
                System.out.println("\u001B[31mNie znaleziono wartosci numerycznej po prefixie: " + this.website.getPrefixIncrementation() + "\u001B[0m");
            }
        }
    }

    private void helpText(String[] args) {
        if (args != null && args.length == 1 && args[0].equals("--help")) {
            System.out.println(fullHelpText());
            System.exit(0);
        } else {
            System.out.println("Wpisz --help aby uzyskac pomoc.");
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.getCause();
        }
    }

    private void check(String tempPage, String oldPage) {
        switch (this.website.getMode()) {
            case PHRASES:
                if (!this.website.getPHRASES().isEmpty()) {
                    for (String phrase : this.website.getPHRASES()) {
                        if ((!this.website.isNegation() && !tempPage.contains(phrase)) || (this.website.isNegation() && tempPage.contains(phrase))) {
                            printDefeatPhrases(tempPage, phrase);
                        } else {
                            printSuccess(phrase);
                        }
                    }
                    System.out.println();
                    sleep(this.website.getInterval() * 1_000);
                }
                return;
            case VALUEBIGGER:
                setActualValue(tempPage);
                if (checkActualValueAgainstThresholdValue()) {
                    printSuccess(null);
                } else {
                    printDefeatAndSleep(tempPage);
                }
                return;
            case VALUESMALLER:
                setActualValue(tempPage);
                if (!checkActualValueAgainstThresholdValue()) {
                    printSuccess(null);
                } else {
                    printDefeatAndSleep(tempPage);
                }
                return;
            case BIGGERTHAN:
                if (tempPage.length() > this.website.getSiteSize()) {
                    printSuccess(null);
                } else {
                    printDefeatAndSleep(tempPage);
                }
                return;
            case SMALLERTHAN:
                if (tempPage.length() < this.website.getSiteSize()) {
                    printSuccess(null);
                } else {
                    printDefeatAndSleep(tempPage);
                }
                return;
            case CHECKVALUE:
                if (tempPage.equals(oldPage)) {
                    printDefeatAndSleep(tempPage);
                } else {
                    printSuccess(null);
                }
                break;
        }
    }

    private String fullHelpText() {
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
                -n (negate) - gdy ustawiona, program bedzie czekal az podane frazy znikna ze strony, nie potrzebuje dodatkowego parametru
                -vb (value bigger) - dwa parametry, pierewszy parametr to prefix przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej w gore bedzie sukces
                -vs (value smaller) - dwa parametry, pierewszy parametr to prefix przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej w dol bedzie sukces
                -inc (increment) - prefix przed wartoscia liczbowa, gdy ustawione, program laduje do szukanych fraz fraze zlozona z prefixu i wartosci zwiekszonej o jeden
                -bt (bigger than) - jesli wartosc strony jest wieksza niz deklarowana wartosc, to sukces
                -st (smaller than) - jesli wartosc strony jest mniejsza niz deklarowana wartosc, to sukces
                -debug - jesli flaga obecna, program wyprintuje zawartosc strony
                Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>""";
    }

    private void argsParsing(String[] args) {
        if (args == null) {
            return;
        }
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    try {
                        this.website.setUrl(new URI(args[i + 1]).toURL());
                    } catch (RuntimeException | URISyntaxException | MalformedURLException e) {
                        errorList.add(
                                "\n\u001B[31mNieprawidlowy parametr URL (" + args[i + 1] + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-i":
                    try {
                        this.website.setInterval(Long.parseLong(args[i + 1]));
                    } catch (Exception e) {
                        errorList.add("\n\u001B[31mNieprawidlowy parametr interwal (" + args[i + 1]
                                + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna\u001B[0m");
                    }
                    break;
                case "-f":
                    try {
                        this.website.setFinish(Long.parseLong(args[i + 1]));
                    } catch (Exception e) {
                        errorList.add(
                                "\n\u001B[31mNieprawidlowy parametr finish (" + args[i + 1] + "), zostanie zignorowany! Uzyta zostanie wartosc domyslna.\u001B[0m");
                    }
                    break;
                case "-e":
                    for (int j = i + 1; j < args.length; j++) {
                        String email = args[j];
                        if (!this.website.getAVAILABLE_PARAMETERS().contains(email)) {
                            if (isValidEmail(email)) {
                                this.website.getEMAILS().add(email);
                            } else {
                                errorList.add("\u001B[31mNieprawidlowy parametr email (" + email + "), zostanie zignorowany!\u001B[0m");
                            }
                        } else {
                            break;
                        }
                    }
                    break;
                case "-s":
                    this.website.setSound(true);
                    break;
                case "-n":
                    this.website.setNegation(true);
                    break;
                case "-p":
                    if (this.website.getMode() == null) this.website.setMode(Util.Mode.PHRASES);
                    for (int j = i + 1; j < args.length; j++) {
                        String phrase = normalizeString(args[j]);
                        if (!this.website.getAVAILABLE_PARAMETERS().contains(phrase)) {
                            this.website.getPHRASES().add(phrase);
                        } else {
                            break;
                        }
                    }
                    break;
                case "-d":
                    try {
                        this.website.setDay(DayOfWeek.valueOf(args[i + 1]));
                    } catch (IllegalArgumentException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr 'day' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-h":
                    try {
                        this.website.setHour(LocalTime.of(Integer.parseInt(args[i + 1]), 0));
                    } catch (NumberFormatException e) {
                        errorList.add("\u001B[31mNieprawidlowy parametr 'hour' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-date":
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    try {
                        this.website.setDate(LocalDate.parse(args[i + 1], formatter));
                    } catch (DateTimeParseException e) {
                        System.out.println("\u001B[31mNieprawidlowy parametr 'date' (" + args[i + 1] + "), zostanie zignorowany!\u001B[0m");
                    }
                    break;
                case "-vb":
                    if (this.website.getMode() == null) {
                        this.website.setMode(Util.Mode.VALUEBIGGER);
                        this.website.setPreValue(normalizeString(args[i + 1]));
                        this.website.setThresholdValue(Float.parseFloat(args[i + 2].replaceAll(",", ".")));
                    }
                    break;
                case "-vs":
                    if (this.website.getMode() == null) {
                        this.website.setMode(Util.Mode.VALUESMALLER);
                        this.website.setPreValue(normalizeString(args[i + 1]));
                        this.website.setThresholdValue(Float.parseFloat(args[i + 2].replaceAll(",", ".")));
                    }
                    break;
                case "-inc":
                    if (this.website.getMode() == null) {
                        this.website.setMode(Util.Mode.PHRASES);
                        this.website.setPrefixIncrementation(normalizeString(args[i + 1]));
                    }
                    break;
                case "-bt":
                    if (this.website.getMode() == null) {
                        this.website.setMode(Util.Mode.BIGGERTHAN);
                        try {
                            this.website.setSiteSize(Long.parseLong(args[i + 1]));
                        } catch (Exception e) {
                            errorList.add("\n\u001B[31mNieprawidlowy parametr siteSize (" + args[i + 1]
                                    + "), zostanie zignorowany!");
                        }
                    }
                    break;
                case "-st":
                    if (this.website.getMode() == null) {
                        this.website.setMode(Util.Mode.SMALLERTHAN);
                        try {
                            this.website.setSiteSize(Long.parseLong(args[i + 1]));
                        } catch (Exception e) {
                            errorList.add("\n\u001B[31mNieprawidlowy parametr siteSize (" + args[i + 1]
                                    + "), zostanie zignorowany!");
                        }
                    }
                    break;
                case "-debug":
                    this.website.setDebug(true);
                    break;
            }
        }
        if (this.website.getMode() == null) this.website.setMode(Util.Mode.CHECKVALUE);
        if (!errorList.isEmpty()) {
            System.out.println();
            for (String error : errorList) {
                System.out.println(error);
            }
        }
    }

    private String normalizeString(String input) {
        input = input.toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", "");
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalizedString).replaceAll("");
    }

    private void checkHour() {
        LocalTime currentTime = LocalTime.now();
        if (!currentTime.isAfter(this.website.getHour())) {
            long secondsDifference = Duration.between(currentTime, this.website.getHour()).abs().getSeconds();
            System.out.println("\u001B[31mNie jest po godzinie " + this.website.getHour() + ", usypiam program na " + secondsDifference / 60 + " minut.\u001B[0m");
            sleep(secondsDifference * 1_000);
        }
    }

    private void checkDay() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (this.website.getDay() != dayOfWeek) {
            System.out.println("\u001B[31mDzis nie jest " + this.website.getDay() + ", dzis jest " + dayOfWeek + "!\u001B[0m");
            exit(30);
        }
    }

    private void exit(long sec) {
        System.out.println("Czekam " + sec + " sekund i zamykam program.");
        sleep(sec * 1_000);
        System.exit(0);
    }

    private void checkDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formattedCurrentDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (currentDate.isBefore(this.website.getDate())) {
            System.out.println(
                    "\u001B[31mDzis nie jest " + formattedCurrentDate.format(this.website.getDate()) + " lub pozniej, dzis jest dopiero " + formattedCurrentDate.format(
                            currentDate) + "!\u001B[0m");
            exit(30);
        }
    }

    private void initialText() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedFinish = decimalFormat.format(this.website.getFinish());
        String initialTxt = "\nPARAMETRY PROGRAMU:";
        initialTxt += "\nStrona: \u001B[35m" + this.website.getUrl() + "\u001B[0m\n";
        initialTxt += "Tryb: \u001B[35m" + this.website.getMode().getLabel() + "\u001B[0m\n";
        initialTxt += "Czestotliwosc odswiezania: \u001B[35m" + this.website.getInterval() + "s \u001B[0m\n";
        initialTxt += "Koniec po: \u001B[35m" + formattedFinish + " iteracjach \u001B[0m\n";

        if (!this.website.getEMAILS().isEmpty()) {
            initialTxt += "Adres/y wysylki emaila: \u001B[35m";
            for (String email : this.website.getEMAILS()) {
                initialTxt = initialTxt.concat(email + ", ");
            }
            initialTxt = initialTxt.substring(0, initialTxt.length() - 2).concat("\u001B[0m\n");
        }
        initialTxt += "Dzwiek: \u001B[35m" + this.website.isSound() + "\u001B[0m\n";
        if (this.website.getDate() != null) {
            initialTxt += "Po dacie: \u001B[35m" + DateTimeFormatter.ofPattern("dd-MM-yyyy").format(this.website.getDate()) + "\u001B[0m\n";
        }
        if (this.website.getDay() != null) {
            initialTxt += "Dzien tygodnia: \u001B[35m" + this.website.getDay() + "\u001B[0m\n";
        }
        if (this.website.getHour() != null) {
            initialTxt += "Godzina: \u001B[35m" + this.website.getHour() + "\u001B[0m\n";
        }
        if (this.website.isNegation()) {
            initialTxt += "Negacja: \u001B[35m" + this.website.isNegation() + "\u001B[0m\n";
        }
        switch (this.website.getMode()) {
            case PHRASES:
                if (!this.website.getPHRASES().isEmpty()) {
                    initialTxt += "Szukane frazy:\n\u001B[35m";
                    for (String phrase : this.website.getPHRASES()) {
                        initialTxt = initialTxt.concat("\u001B[35m" + phrase + "\u001B[0m\n");
                    }
                }
                break;
            case VALUEBIGGER:
            case VALUESMALLER:
                if (this.website.getPHRASES().isEmpty()) {
                    initialTxt += "Szukanie wartosci";
                    if (this.website.getMode() == Util.Mode.VALUEBIGGER) initialTxt += " wiekszej";
                    if (this.website.getMode() == Util.Mode.VALUESMALLER) initialTxt += " mniejszej";
                    initialTxt += "niz: \u001B[35m" + this.website.getThresholdValue() + "\u001B[0m\n";
                }
                break;
            case BIGGERTHAN:
                initialTxt += "Strona ma byc wieksza niz: \u001B[35m" + this.website.getSiteSize() + "\u001B[0m\n";
                break;
            case SMALLERTHAN:
                initialTxt += "Strona ma byc mniejsza niz: \u001B[35m" + this.website.getSiteSize() + "\u001B[0m\n";
                break;
        }
        if (this.website.getDate() != null) {
            checkDate();
        }
        if (this.website.getDay() != null) {
            checkDay();
        }
        if (this.website.getHour() != null) {
            checkHour();
        }
        System.out.println(initialTxt.substring(0, initialTxt.length() - 1));
    }

    private String connection(URL url) {
        StringBuilder content = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Nagłówki identyczne jak z curl
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "identity"); // żeby uniknąć gzip

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                String redirectUrl = conn.getHeaderField("Location");
                try {
                    return connection(new URI(redirectUrl).toURL());
                } catch (URISyntaxException e) {
                    System.out.println("Nieprawidlowy URL przekierowania: " + redirectUrl);
                    return "";
                }
            } else if (responseCode != 200) {
                System.out.println("Blad HTTP: " + responseCode);
                return "";
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

        } catch (IOException e) {
            System.out.println("Nie udalo sie polaczyc z podanym adresem: " + e.getMessage());
            return "";
        }
        return normalizeString(content.toString());
    }

    private void initialEmptyPageProtection(int i) {
        System.out.print("Pusta strona... Prawdopodobnie zly adres lub brak internetu...");
        if (i > this.website.getEMPTY_PAGE_RETRIES()) {
            exit(30);
        }
        System.out.println(" Ponawiam probe za 30s. (" + i + "/" + this.website.getEMPTY_PAGE_RETRIES() + ")");
        sleep(30_000);
    }

    private boolean emptyPageProtection(int number, String tempPage) {
        if (tempPage.isEmpty()) {
            System.out.println(getTime() + " - " + number + " proba - Pusta strona... Prawdopodobnie chwilowy brak internetu lub blad serwera.");
            return true;
        }
        return false;
    }

    private String getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return now.format(formatter);
    }

    private boolean checkActualValueAgainstThresholdValue() {
        return this.website.getActualValue().compareTo(this.website.getThresholdValue()) > 0;
    }

    private void setActualValue(String page) {
        int position = page.indexOf(this.website.getPreValue());
        if (position == -1) {
            System.out.println("Fragment '" + this.website.getPreValue() + "' nie zostal znaleziony na stronie.");
            exit(30);
        }
        float number = 0;
        int endPos = Math.min(position + this.website.getPreValue().length() + 20, page.length());
        String text = page.substring(position + this.website.getPreValue().length(), endPos);
        Pattern pattern = Pattern.compile("[0-9,.]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String numberStr = matcher.group();
            int commaCounter = countChar(numberStr, ',');
            int dotCounter = countChar(numberStr, '.');
            if (commaCounter > dotCounter) {
                numberStr = numberStr.replaceAll(",", "");
            } else if (commaCounter < dotCounter) {
                numberStr = numberStr.replaceAll("\\.", "");
            } else if (commaCounter > 0) {
                if (numberStr.indexOf(",") > numberStr.indexOf(".")) {
                    numberStr = numberStr.replaceAll("\\.", "");
                } else {
                    numberStr = numberStr.replaceAll(",", "");
                }
            }
            number = Float.parseFloat(numberStr);
        }
        this.website.setActualValue(number);
    }

    private int countChar(String text, char ch) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    private void playSound(int repeats) {
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
                System.out.println("Blad podczas odtwarzania dzwieku: " + e.getMessage());
            }
        }
    }

    private void printDefeatAndSleep(String tempPage) {
        String result = "\u001B[32m" + getTime() + " - ";
        switch (this.website.getMode()) {
            case Util.Mode.VALUEBIGGER:
                result += "Znaleziona wartosc: '" + this.website.getActualValue() + "', nie jest wieksza niz ustawiona wartosc progowa: '" + this.website.getThresholdValue() + "'";
                break;
            case Util.Mode.VALUESMALLER:
                result += "Znaleziona wartosc: '" + this.website.getActualValue() + "', nie jest mniejsza niz ustawiona wartosc progowa: '" + this.website.getThresholdValue() + "'";
                break;
            case Util.Mode.BIGGERTHAN:
                result += "Wielkosc strony: '" + tempPage.length() + "', nie jest wieksza niz ustawiona wartosc progowa: '" + this.website.getSiteSize() + "'\u001B[0m";
                break;
            case Util.Mode.SMALLERTHAN:
                result += "Wielkosc strony: '" + tempPage.length() + "', nie jest mniejsza niz ustawiona wartosc progowa: '" + this.website.getSiteSize() + "'\u001B[0m";
                break;
            case Util.Mode.CHECKVALUE:
                result += "Brak zmiany strony.";
                break;
        }
        if (this.website.getMode() != Util.Mode.SMALLERTHAN && this.website.getMode() != Util.Mode.BIGGERTHAN) {
            System.out.println(result += "- wielkosc strony: " + tempPage.length() + "\u001B[0m");
        }
        System.out.println(result);
        sleep(this.website.getInterval() * 1_000);
    }

    private void printDefeatPhrases(String tempPage, String phrase) {
        String result = "\u001B[32m" + getTime() + " - szukam ";
        if (this.website.isNegation()) {
            result += "braku ";
        }
        result += "tekstu: " + phrase + "... ";
        System.out.println(result + "- wielkosc strony: " + tempPage.length() + "\u001B[0m");
    }

    private void printSuccess(String phrase) {
        String result = "\u001B[01;41m" + getTime() + " - SUKCES - ";
        switch (this.website.getMode()) {
            case PHRASES:
                if (!this.website.getPHRASES().isEmpty()) {
                    if (this.website.isNegation()) {
                        result += "nie znaleziono frazy: ";
                    } else {
                        result += "znaleziono fraze: ";
                    }
                    result += phrase;
                }
                break;
            case VALUEBIGGER:
            case VALUESMALLER:
                result += "Znaleziona wartosc: " + this.website.getActualValue() + " jest ";
                if (checkActualValueAgainstThresholdValue()) {
                    result += "wieksza";
                } else {
                    result += "mniejsza";
                }
                result += " niz ustawiona wartosc progowa: " + this.website.getThresholdValue();
                break;
            case BIGGERTHAN:
                result += "Wielkosc strony: " + this.website.getTempPage().length() + " jest wieksza niz ustawiona wartosc progowa: " + this.website.getSiteSize();
                break;
            case SMALLERTHAN:
                result += "Wielkosc strony: " + this.website.getTempPage().length() + " jest mniejsza niz ustawiona wartosc progowa: " + this.website.getSiteSize();
                break;
            case CHECKVALUE:
                result += "jest zmiana strony";
                break;
        }

        System.out.println(result + "\n\u001B[0m");
        if (!this.website.getEMAILS().isEmpty()) {
            for (String email : this.website.getEMAILS()) {
                sendMail(email, this.website.getUrl(), phrase, 1, 5);
            }
        }
        if (this.website.isSound()) {
            playSound(10_000);
        }
        exit(3_600);
    }

    private void sendMail(String email, URL urlString, String searchedPhrase, int retries, int retriesLimit) {
        if (this.website.isSound()) {
            playSound(3);
        }
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
                if (this.website.isNegation()) {
                    txt += "Nie znaleziono textu: " + searchedPhrase;
                } else {
                    txt += "Znaleziono text: " + searchedPhrase;
                }
            }
            message.setText(txt);
            Transport.send(message);
            System.out.println("Email na adres " + email + " zostal wyslany pomyslnie.");

        } catch (Exception e) {
            if (retries <= retriesLimit) {
                System.out.println("Wystapil blad podczas wysylania emaila: '" + e.getMessage() + "', ponawiam probe wyslania maila, proba nr " + retries + "/"
                        + retriesLimit);
                sleep(30_000);
                sendMail(email, urlString, searchedPhrase, ++retries, retriesLimit);
            }
        }
    }

    private boolean isValidEmail(String email) {
        Matcher matcher = Pattern.compile(this.website.getEMAIL_REGEX()).matcher(email);
        return matcher.matches();
    }
}
