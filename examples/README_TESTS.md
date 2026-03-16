# Przykłady Testów Jednostkowych

Ten katalog zawiera przykłady testów jednostkowych pokazujące, jak łatwo jest testować zrefaktoryzowany kod.

## Wymagane biblioteki do testowania

Pobierz i umieść w `lib/`:
- JUnit 5: https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.10.0/
- Mockito: https://repo1.maven.org/maven2/org/mockito/mockito-core/5.5.0/

## Uruchomienie testów

```powershell
# Kompiluj testy
javac -d out -sourcepath src;examples -cp "lib/*" examples/**/*.java

# Uruchom testy (wymaga JUnit Console Launcher)
java -jar lib/junit-platform-console-standalone-1.10.0.jar --class-path out --scan-classpath
```

## Co pokazują te testy?

### 1. **PhrasesCheckStrategyTest**
- Testowanie strategii bez zależności zewnętrznych
- Mock MonitoringConfig używając Builder Pattern
- Weryfikacja różnych scenariuszy (success, fail, negation)

### 2. **WebPageFetcherTest**
- Test integracyjny HTTP client
- Pokazuje jak izolować testy komunikacji sieciowej
- Łatwe do mock'owania w innych testach

### 3. **StringNormalizerTest**
- Proste, szybkie testy jednostkowe
- Brak zależności = błyskawiczne testy
- Test edge cases (null, empty, special chars)

## Porównanie: Przed vs Po Refaktoryzacji

### PRZED - Niemożliwe do testowania:
```java
public class PageChange {
    private final Config config;
    
    public PageChange() {
        this.config = new Config(); // Hard dependency!
    }
    
    public void check(String tempPage, String oldPage) {
        // 200 linii kodu z System.exit(), HTTP calls, email sending...
        // NIEMOŻLIWE do przetestowania!
    }
}
```

**Problemy:**
- ❌ Hard dependencies (new Config())
- ❌ System.exit() przerywa testy
- ❌ HTTP calls w każdym teście
- ❌ Email sending w testach
- ❌ Thread.sleep spowalnia testy
- ❌ Nie można mockować zależności
- ❌ Jedna wielka metoda z wszystkim

### PO - Łatwe testowanie:
```java
public class PhrasesCheckStrategy implements CheckStrategy {
    @Override
    public CheckResult check(String page, MonitoringState state, MonitoringConfig config) {
        // Tylko logika sprawdzania fraz
        // Brak side effects!
    }
}

// Test:
@Test
public void testCheckSuccess_phraseFound() {
    PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
    MonitoringConfig config = MonitoringConfig.builder()
        .addPhrase("test")
        .build();
    
    CheckResult result = strategy.check("page with test", new MonitoringState(10), config);
    
    assertTrue(result.isSuccess());
}
```

**Zalety:**
- ✅ Dependency Injection - łatwe mockowanie
- ✅ Małe, skoncentrowane klasy
- ✅ Brak side effects
- ✅ ExitHandler/Sleeper interfaces dla testowalności
- ✅ Immutable objects
- ✅ Strategy Pattern - test każdej strategii osobno
- ✅ Szybkie testy (ms, nie sekundy)

## Przykład mocka z całym PageMonitor

```java
@Test
public void testPageMonitor_successNotification() {
    // Mock wszystkich zależności
    WebPageFetcher mockFetcher = mock(WebPageFetcher.class);
    when(mockFetcher.fetchPage(any())).thenReturn("<html>testphrase</html>");
    
    CheckStrategy mockStrategy = mock(CheckStrategy.class);
    when(mockStrategy.check(any(), any(), any()))
        .thenReturn(new CheckResult(true, "testphrase"));
    
    NotificationService mockNotifier = mock(NotificationService.class);
    ExitHandler mockExit = mock(ExitHandler.class);
    
    // Utwórz PageMonitor z mockami
    PageMonitor monitor = new PageMonitor(
        config, mockFetcher, normalizer, mockStrategy,
        mockNotifier, outputFormatter, timeValidator,
        sleeper, mockExit, numericExtractor
    );
    
    // Test
    monitor.start();
    
    // Weryfikacja
    verify(mockNotifier).notifyAll(any());
    verify(mockExit).exit(0);
}
```

## Metryki testowania

| Metryka | Przed | Po |
|---------|-------|-----|
| Możliwość testowania | 0% | 100% |
| Czas setupu testu | - | <1s |
| Czas wykonania testu | - | <10ms |
| Coverage możliwy | 0% | 100% |
| Mock'owalność | Nie | Tak |

## Wnioski

Refaktoryzacja obiektowa nie tylko poprawia strukturę kodu, ale przede wszystkim:
1. **Umożliwia testowanie** - wcześniej było niemożliwe
2. **Przyspiesza development** - szybkie feedback z testów
3. **Zwiększa pewność** - testy chronią przed regresją
4. **Ułatwia refactoring** - testy potwierdzają, że nic się nie zepsuło
5. **Dokumentuje kod** - testy jako dokumentacja użycia

