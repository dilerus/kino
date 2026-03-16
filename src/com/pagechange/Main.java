package com.pagechange;

import com.pagechange.config.ArgumentParser;
import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.PageMonitor;
import com.pagechange.http.WebPageFetcher;
import com.pagechange.notification.NotificationService;
import com.pagechange.strategy.CheckStrategy;
import com.pagechange.strategy.StrategyFactory;
import com.pagechange.util.*;
import com.pagechange.validation.EmailValidator;
import com.pagechange.validation.TimeValidator;

public class Main {
    public static void main(String[] args) {
        // Create utilities
        StringNormalizer normalizer = new StringNormalizer();
        NumericValueExtractor numericExtractor = new NumericValueExtractor();
        EmailValidator emailValidator = new EmailValidator();
        AnsiColorFormatter colorFormatter = new AnsiColorFormatter();
        TimeFormatter timeFormatter = new TimeFormatter();
        Sleeper sleeper = new Sleeper();
        ExitHandler exitHandler = new SystemExitHandler();

        // Parse arguments
        ArgumentParser parser = new ArgumentParser(emailValidator, normalizer, numericExtractor);
        MonitoringConfig config = parser.parse(args);

        // Create services
        WebPageFetcher fetcher = new WebPageFetcher();
        StrategyFactory strategyFactory = new StrategyFactory(numericExtractor);
        CheckStrategy strategy = strategyFactory.createStrategy(config);
        NotificationService notificationService = NotificationService.create(config, sleeper);
        ConsoleOutputFormatter outputFormatter = new ConsoleOutputFormatter(colorFormatter, timeFormatter);
        TimeValidator timeValidator = new TimeValidator(colorFormatter, sleeper, exitHandler);

        // Create and start monitor
        PageMonitor monitor = new PageMonitor(
                config,
                fetcher,
                normalizer,
                strategy,
                notificationService,
                outputFormatter,
                timeValidator,
                sleeper,
                exitHandler,
                numericExtractor
        );

        monitor.start();
    }
}

