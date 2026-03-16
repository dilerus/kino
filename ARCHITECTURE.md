# Architektura PageChange - Diagram

## Przepływ Danych

```
┌─────────────────────────────────────────────────────────────────────┐
│                              Main.java                              │
│                        (Dependency Injection)                       │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  │ creates & injects
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           PageMonitor                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ Dependencies:                                                 │  │
│  │ • MonitoringConfig (immutable)                              │  │
│  │ • WebPageFetcher                                            │  │
│  │ • CheckStrategy (from StrategyFactory)                      │  │
│  │ • NotificationService                                       │  │
│  │ • StringNormalizer, TimeValidator, etc.                     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
          │                    │                    │
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────┐  ┌──────────────────┐  ┌───────────────────┐
│ WebPageFetcher  │  │  CheckStrategy   │  │ NotificationServ. │
│                 │  │  (Strategy Pat.) │  │                   │
│ • fetchPage()   │  │                  │  │ • notifyAll()     │
│ • handles       │  │ Implementations: │  │                   │
│   redirects     │  │ • PhrasesCheck   │  │ Contains:         │
│ • normalizes    │  │ • ValueBigger    │  │ • EmailNotifier   │
│   via String    │  │ • ValueSmaller   │  │ • SoundNotifier   │
│   Normalizer    │  │ • CheckValue     │  │                   │
│                 │  │ • SiteBigger     │  │ Uses:             │
│                 │  │ • SiteSmaller    │  │ • Sleeper         │
│                 │  │                  │  │ • SoundNotifier   │
└─────────────────┘  └──────────────────┘  └───────────────────┘

```

## Struktura Pakietów z Zależnościami

```
com.pagechange
│
├── Main.java ─────────────────────┐
│                                   │
├���─ core/                           │
│   ├── PageMonitor ◄───────────────┤ (używa wszystkich)
│   └── MonitoringState             │
│                                   │
├── config/                         │
│   ├── MonitoringConfig ◄──────────┤
│   ├── MonitoringMode              │
│   ├── AppConstants                │
│   └── ArgumentParser ◄────────────┤
│                                   │
├── http/                           │
│   └── WebPageFetcher ◄────────────┤
│       └── uses: StringNormalizer  │
│                                   │
├── strategy/ (Strategy Pattern)    │
│   ├── CheckStrategy (interface) ◄─┤
│   ├── CheckResult                 │
│   ├── PhrasesCheckStrategy        │
│   ├── ValueBiggerStrategy         │
│   ├── ValueSmallerStrategy        │
│   ├── SiteBiggerThanStrategy      │
│   ├── SiteSmallerThanStrategy     │
│   ├── CheckValueStrategy          │
│   └── StrategyFactory ◄───────────┤
│                                   │
├── notification/                   │
│   ├── Notifier (interface) ◄──────┤
│   ├── NotificationContext         │
│   ├── NotificationService ◄───────┤
│   ├── EmailNotifier               │
│   └── SoundNotifier               │
│                                   │
├── validation/                     │
│   ├── ValidationError ◄───────────┤
│   ├── EmailValidator              │
│   └── TimeValidator ◄─────────────┤
│                                   │
└── util/                           │
    ├── AnsiColorFormatter ◄───────┤
    ├── StringNormalizer ◄─────────┤
    ├── NumericValueExtractor ◄────┤
    ├── TimeFormatter ◄────────────┤
    ├── Sleeper ◄──────────────────┤
    ├── ExitHandler (interface) ◄──┤
    ├── SystemExitHandler ◄────────┤
    └── ConsoleOutputFormatter ◄───┘
```

## Sekwencja Działania

```
1. START
   ↓
2. Main: Parse arguments → MonitoringConfig (immutable)
   ↓
3. Main: Create utilities (Normalizer, Formatter, Validator, etc.)
   ↓
4. Main: StrategyFactory.createStrategy(config) → CheckStrategy
   ↓
5. Main: NotificationService.create(config) → NotificationService
   ↓
6. Main: new PageMonitor(all dependencies via DI)
   ↓
7. PageMonitor.start()
   ├─→ TimeValidator: validate date/day/hour
   ├─→ WebPageFetcher: fetch initial page (retry 5x if empty)
   ├─→ NumericValueExtractor: load incrementation phrase (if -inc)
   └─→ runMonitoringLoop()
       │
       └─→ LOOP (finish iterations):
           ├─→ WebPageFetcher: fetch current page
           ├─→ StringNormalizer: normalize page
           ├─→ CheckStrategy: check(page, state, config) → CheckResult
           │   └─→ Different strategy based on MonitoringMode
           ├─→ if SUCCESS:
           │   ├─→ ConsoleOutputFormatter: printSuccess()
           │   ├─→ NotificationService: notifyAll()
           │   │   ├─→ EmailNotifier: send email (retry 5x)
           │   │   └─→ SoundNotifier: play sound (loop)
           │   ├─→ Sleeper: sleep(3600s)
           │   └─→ ExitHandler: exit(0)
           └─→ if FAIL:
               ├─→ ConsoleOutputFormatter: printDefeat()
               └─→ Sleeper: sleep(interval)
```

## Strategy Pattern w Akcji

```
MonitoringConfig.mode → StrategyFactory → CheckStrategy

┌──────────────────┐
│ MonitoringMode   │
├──────────────────┤
│ • CHECK_VALUE    │ → CheckValueStrategy
│ • PHRASES        │ → PhrasesCheckStrategy
│ • VALUE_BIGGER   │ → ValueBiggerStrategy (uses NumericValueExtractor)
│ • VALUE_SMALLER  │ → ValueSmallerStrategy (uses NumericValueExtractor)
│ • SITE_BIGGER    │ → SiteBiggerThanStrategy
│ • SITE_SMALLER   │ → SiteSmallerThanStrategy
└──────────────────┘

Każda strategia:
• check(page, state, config) → CheckResult
• getSuccessMessage() → String
• getDefeatMessage() → String
```

## Notification Flow

```
SUCCESS detected
    ↓
NotificationService.notifyAll(context)
    ↓
    ├─→ EmailNotifier (if emails configured)
    │   ├─→ SoundNotifier.notify() (3x before email)
    │   ├─→ Build SMTP properties
    │   ├─→ Create MimeMessage
    │   ├─→ Transport.send()
    │   └─→ Retry 5x with 30s delay if fail
    │
    └─→ SoundNotifier (if sound enabled)
        └─→ Play tada.wav (10,000x in loop)
```

## Dependency Injection w Main.java

```java
// 1. Create utilities (zero dependencies)
StringNormalizer normalizer = new StringNormalizer();
NumericValueExtractor extractor = new NumericValueExtractor();
EmailValidator emailValidator = new EmailValidator();
AnsiColorFormatter colorFormatter = new AnsiColorFormatter();
TimeFormatter timeFormatter = new TimeFormatter();
Sleeper sleeper = new Sleeper();
ExitHandler exitHandler = new SystemExitHandler();

// 2. Parse config (uses utilities)
ArgumentParser parser = new ArgumentParser(emailValidator, normalizer, extractor);
MonitoringConfig config = parser.parse(args);

// 3. Create services (uses utilities)
WebPageFetcher fetcher = new WebPageFetcher();
StrategyFactory strategyFactory = new StrategyFactory(extractor);
CheckStrategy strategy = strategyFactory.createStrategy(config);
NotificationService notificationService = NotificationService.create(config, sleeper);
ConsoleOutputFormatter outputFormatter = new ConsoleOutputFormatter(colorFormatter, timeFormatter);
TimeValidator timeValidator = new TimeValidator(colorFormatter, sleeper, exitHandler);

// 4. Create main component (inject ALL dependencies)
PageMonitor monitor = new PageMonitor(
    config, fetcher, normalizer, strategy, notificationService,
    outputFormatter, timeValidator, sleeper, exitHandler, extractor
);

// 5. Run
monitor.start();
```

## Kluczowe Zasady Architektury

### 1. **Separation of Concerns**
- HTTP logic → `http/`
- Notification logic → `notification/`
- Validation logic → `validation/`
- Utilities → `util/`
- Core business logic → `core/`

### 2. **Dependency Direction**
```
Main → PageMonitor → Services → Utilities
     ↘ Config     ↘ Strategies
```
Wszystkie zależności idą w jedną stronę (no circular dependencies)

### 3. **Interface Segregation**
- `Notifier` - dla różnych typów powiadomień
- `CheckStrategy` - dla różnych strategii sprawdzania
- `ExitHandler` - dla testowalności

### 4. **Immutability**
- `MonitoringConfig` - immutable z Builder
- `CheckResult` - immutable value object
- `NotificationContext` - immutable value object
- `ValidationError` - immutable

### 5. **Single Responsibility**
Każda klasa robi JEDNĄ rzecz:
- `WebPageFetcher` - tylko HTTP
- `EmailNotifier` - tylko email
- `StringNormalizer` - tylko normalizacja
- `ArgumentParser` - tylko parsing
- etc.

## Porównanie Architektury

### PRZED
```
PageChange (699 lines)
├── all HTTP logic
├── all validation logic
├── all notification logic
├── all strategy logic
├── all parsing logic
└── all utility logic
    └─→ MONOLITH
```

### PO
```
Main (DI Container)
├── core/ (business logic)
├── config/ (configuration)
├── http/ (external communication)
├── strategy/ (algorithms)
├── notification/ (alerts)
├── validation/ (checks)
└── util/ (helpers)
    └─→ MODULAR, TESTABLE, MAINTAINABLE
```

## Zalety Nowej Architektury

1. **Testowalność** - każdy komponent osobno
2. **Rozszerzalność** - nowe strategie/notyfikatory bez zmian
3. **Czytelność** - jasny podział odpowiedzialności
4. **Utrzymywalność** - małe, skoncentrowane klasy
5. **Reużywalność** - komponenty wielokrotnego użytku
6. **Skalowalność** - łatwe dodawanie funkcjonalności
7. **SOLID** - wszystkie zasady zastosowane

