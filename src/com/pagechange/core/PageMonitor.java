package com.pagechange.core;

import com.pagechange.config.AppConstants;
import com.pagechange.config.MonitoringConfig;
import com.pagechange.config.MonitoringMode;
import com.pagechange.http.WebPageFetcher;
import com.pagechange.notification.NotificationContext;
import com.pagechange.notification.NotificationService;
import com.pagechange.strategy.CheckResult;
import com.pagechange.strategy.CheckStrategy;
import com.pagechange.strategy.PhrasesCheckStrategy;
import com.pagechange.util.*;
import com.pagechange.validation.TimeValidator;

public class PageMonitor {
    private final MonitoringConfig config;
    private final WebPageFetcher fetcher;
    private final StringNormalizer normalizer;
    private final CheckStrategy strategy;
    private final NotificationService notificationService;
    private final ConsoleOutputFormatter outputFormatter;
    private final TimeValidator timeValidator;
    private final Sleeper sleeper;
    private final ExitHandler exitHandler;
    private final NumericValueExtractor numericExtractor;

    public PageMonitor(MonitoringConfig config,
                      WebPageFetcher fetcher,
                      StringNormalizer normalizer,
                      CheckStrategy strategy,
                      NotificationService notificationService,
                      ConsoleOutputFormatter outputFormatter,
                      TimeValidator timeValidator,
                      Sleeper sleeper,
                      ExitHandler exitHandler,
                      NumericValueExtractor numericExtractor) {
        this.config = config;
        this.fetcher = fetcher;
        this.normalizer = normalizer;
        this.strategy = strategy;
        this.notificationService = notificationService;
        this.outputFormatter = outputFormatter;
        this.timeValidator = timeValidator;
        this.sleeper = sleeper;
        this.exitHandler = exitHandler;
        this.numericExtractor = numericExtractor;
    }

    public void start() {
        // Validate time constraints
        timeValidator.validateDate(config.getDate());
        timeValidator.validateDay(config.getDay());
        timeValidator.validateHour(config.getHour());

        // Print configuration
        outputFormatter.printInitialConfiguration(config);

        // Fetch initial page
        String initialPage = fetchInitialPage();

        // Load incrementation phrase if needed
        if (config.getMode() == MonitoringMode.PHRASES && config.getPrefixIncrementation() != null) {
            loadIncrementationPhrase(initialPage);
        }

        // Start monitoring loop
        MonitoringState state = new MonitoringState(config.getFinish());
        state.setPreviousPage(initialPage);

        runMonitoringLoop(state);
    }

    private String fetchInitialPage() {
        for (int i = 1; i <= AppConstants.EMPTY_PAGE_RETRIES; i++) {
            String page = fetcher.fetchPage(config.getUrl());
            String normalizedPage = normalizer.normalize(page);

            if (!normalizedPage.isEmpty()) {
                return normalizedPage;
            }

            outputFormatter.printInitialEmptyPageWarning(i, AppConstants.EMPTY_PAGE_RETRIES);
            sleeper.sleep(AppConstants.EMPTY_PAGE_RETRY_DELAY_MS);
        }

        exit(AppConstants.EXIT_DELAY_SECONDS);
        return "";
    }

    private void loadIncrementationPhrase(String page) {
        String prefix = config.getPrefixIncrementation();
        Long value = numericExtractor.extractLongValue(page, prefix);

        if (value != null) {
            value++;
            String incrementedPhrase = prefix + value;

            // Add to config (need to rebuild)
            MonitoringConfig.Builder builder = MonitoringConfig.builder()
                    .url(config.getUrl())
                    .interval(config.getInterval())
                    .finish(config.getFinish())
                    .emails(config.getEmails())
                    .sound(config.isSound())
                    .negation(config.isNegation())
                    .day(config.getDay())
                    .hour(config.getHour())
                    .date(config.getDate())
                    .debug(config.isDebug())
                    .mode(config.getMode())
                    .phrases(config.getPhrases())
                    .addPhrase(incrementedPhrase)
                    .preValue(config.getPreValue())
                    .thresholdValue(config.getThresholdValue())
                    .siteSize(config.getSiteSize())
                    .prefixIncrementation(config.getPrefixIncrementation());

            if (config.getPhrases().isEmpty()) {
                System.out.println("Szukane frazy:");
            }
            outputFormatter.printIncrementedPhrase(incrementedPhrase);
        } else {
            System.out.println("\u001B[31mNie znaleziono wartosci numerycznej po prefixie: " + prefix + "\u001B[0m");
        }
    }

    private void runMonitoringLoop(MonitoringState state) {
        int emptyPageCounter = 1;

        while (state.hasIterationsRemaining()) {
            String rawPage = fetcher.fetchPage(config.getUrl());
            String currentPage = normalizer.normalize(rawPage);

            if (currentPage.isEmpty()) {
                outputFormatter.printEmptyPageWarning(emptyPageCounter++, AppConstants.EMPTY_PAGE_RETRIES);
                sleeper.sleep(config.getInterval() * 1_000);
                continue;
            }

            emptyPageCounter = 1;
            state.setCurrentPage(currentPage);

            if (config.isDebug()) {
                System.out.println(currentPage);
            }

            processPage(currentPage, state);

            state.setPreviousPage(currentPage);
            state.decrementIterations();
        }

        System.out.println("Wartosc parametru 'finish' doszla do 0.");
        exit(AppConstants.EXIT_DELAY_SECONDS);
    }

    private void processPage(String currentPage, MonitoringState state) {
        if (config.getMode() == MonitoringMode.PHRASES) {
            processPhrasesMode(currentPage, state);
        } else {
            processNonPhrasesMode(currentPage, state);
        }
    }

    private void processPhrasesMode(String currentPage, MonitoringState state) {
        PhrasesCheckStrategy phrasesStrategy = (PhrasesCheckStrategy) strategy;
        boolean anySuccess = false;

        for (String phrase : config.getPhrases()) {
            boolean containsPhrase = currentPage.contains(phrase);
            boolean success = config.isNegation() ? !containsPhrase : containsPhrase;

            if (success) {
                String message = phrasesStrategy.getSuccessMessageForPhrase(phrase, config);
                outputFormatter.printSuccess(message);
                notifySuccess(phrase, state.getActualValue());
                anySuccess = true;
            } else {
                String message = phrasesStrategy.getDefeatMessageForPhrase(phrase, config, currentPage);
                outputFormatter.printDefeat(message);
            }
        }

        if (!anySuccess) {
            System.out.println();
            sleeper.sleep(config.getInterval() * 1_000);
        }
    }

    private void processNonPhrasesMode(String currentPage, MonitoringState state) {
        CheckResult result = strategy.check(currentPage, state, config);

        if (result.isSuccess()) {
            String message = strategy.getSuccessMessage(state, config);
            outputFormatter.printSuccess(message);
            notifySuccess(result.getMatchedPhrase(), state.getActualValue());
        } else {
            String message = strategy.getDefeatMessage(state, config, currentPage);
            outputFormatter.printDefeat(message);
            sleeper.sleep(config.getInterval() * 1_000);
        }
    }

    private void notifySuccess(String phrase, Float actualValue) {
        NotificationContext context = new NotificationContext(config, phrase, actualValue);
        notificationService.notifyAll(context);
        exit(AppConstants.SUCCESS_EXIT_DELAY_SECONDS);
    }

    private void exit(long seconds) {
        System.out.println("Czekam " + seconds + " sekund i zamykam program.");
        sleeper.sleep(seconds * 1_000);
        exitHandler.exit(0);
    }
}

