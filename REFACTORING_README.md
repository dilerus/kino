# PageChange - Refaktoryzacja Obiektowa

## Biblioteki Zewnętrzne

Projekt wymaga następujących bibliotek:

### 1. javax.mail (JavaMail API)
Pobierz z Maven Central:
- https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar

Lub użyj Maven/Gradle:
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

Umieść plik JAR w katalogu `lib/`

### 2. Lombok (opcjonalnie, jeśli chcesz używać @Getter/@Setter)
- https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar

## Kompilacja

```powershell
# Pobierz javax.mail-1.6.2.jar i umieść w katalogu lib/

# Kompilacja
javac -d out -sourcepath src -cp "lib/*" src/com/pagechange/**/*.java

# Uruchomienie
java -cp "out;lib/*" com.pagechange.Main --help
```

## Tworzenie JAR

```powershell
# Skopiuj resources
Copy-Item -Recurse src/resources out/

# Skopiuj MANIFEST.MF
Copy-Item src/META-INF/MANIFEST.MF out/META-INF/

# Utwórz JAR
jar cfm pageChange.jar out/META-INF/MANIFEST.MF -C out .

# Uruchom
java -jar pageChange.jar --help
```

## Struktura Projektu

```
com.pagechange/
├── Main.java                      # Punkt wejścia aplikacji
├── core/
│   ├── PageMonitor.java          # Główna pętla monitorowania
│   └── MonitoringState.java      # Stan monitorowania (runtime data)
├── config/
│   ├── AppConstants.java         # Stałe aplikacji
│   ├── MonitoringConfig.java    # Immutable konfiguracja
│   ├── MonitoringMode.java       # Enum trybów sprawdzania
│   └── ArgumentParser.java       # Parser argumentów CLI
├── http/
│   └── WebPageFetcher.java       # HTTP client
├── strategy/
│   ├── CheckStrategy.java        # Interface dla strategii
│   ├── CheckResult.java          # Wynik sprawdzenia
│   ├── CheckValueStrategy.java   # Sprawdzanie zmiany
│   ├── PhrasesCheckStrategy.java # Sprawdzanie fraz
│   ├── ValueBiggerStrategy.java  # Sprawdzanie wartości >
│   ├── ValueSmallerStrategy.java # Sprawdzanie wartości <
│   ├── SiteBiggerThanStrategy.java
│   ├── SiteSmallerThanStrategy.java
│   └── StrategyFactory.java      # Factory dla strategii
├── notification/
│   ├── Notifier.java             # Interface powiadomień
│   ├── NotificationContext.java  # Kontekst powiadomienia
│   ├── NotificationService.java  # Service agregujący powiadomienia
│   ├── EmailNotifier.java        # Wysyłanie email
│   └── SoundNotifier.java        # Odtwarzanie dźwięku
├── validation/
│   ├── ValidationError.java      # Błąd walidacji
│   ├── EmailValidator.java       # Walidator email
│   └── TimeValidator.java        # Walidator czasu/daty
└── util/
    ├── AnsiColorFormatter.java   # Formatowanie kolorów ANSI
    ├── StringNormalizer.java     # Normalizacja tekstu
    ├── NumericValueExtractor.java # Ekstrakcja wartości numerycznych
    ├── TimeFormatter.java        # Formatowanie czasu
    ├── Sleeper.java              # Wrapper dla Thread.sleep
    ├── ExitHandler.java          # Interface dla System.exit
    ├── SystemExitHandler.java    # Implementacja ExitHandler
    └── ConsoleOutputFormatter.java # Formatowanie outputu konsoli
```

## Zmiany Obiektowe

### 1. **Single Responsibility Principle (SRP)**
- Każda klasa ma jedną odpowiedzialność
- `PageChange` został podzielony na 30+ klas

### 2. **Strategy Pattern**
- Różne tryby sprawdzania (`-p`, `-vb`, `-vs`, etc.) to osobne strategie
- Eliminacja dużych switch/case

### 3. **Dependency Injection**
- Wszystkie zależności wstrzykiwane przez konstruktory
- Ułatwia testowanie i mockowanie

### 4. **Immutable Configuration**
- `MonitoringConfig` jest immutable z Builder pattern
- Bezpieczeństwo danych, thread-safety

### 5. **Separation of Concerns**
- HTTP w osobnym pakiecie
- Notyfikacje w osobnym pakiecie
- Walidacja w osobnym pakiecie
- Utilities w osobnym pakiecie

### 6. **Interface Segregation**
- `Notifier` interface dla różnych typów powiadomień
- `CheckStrategy` interface dla różnych strategii
- `ExitHandler` interface dla testowalności

### 7. **Factory Pattern**
- `StrategyFactory` tworzy odpowiednie strategie
- `NotificationService.create()` tworzy powiadomienia

### 8. **Value Objects & Wrappers**
- `CheckResult` enkapsuluje wynik sprawdzenia
- `NotificationContext` enkapsuluje kontekst powiadomienia
- `ValidationError` zamiast prostego String

### 9. **Lepsze nazewnictwo**
- `Error` → `ValidationError`
- `Config` → `MonitoringConfig` + `AppConstants`
- Jasne, opisowe nazwy klas i metod

### 10. **Testowalność**
- Wszystkie zależności można mockować
- Brak `new` wewnątrz klas (DI)
- Interface dla System.exit i Thread.sleep

