package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.config.MonitoringMode;
import com.pagechange.util.NumericValueExtractor;

public class StrategyFactory {
    private final NumericValueExtractor extractor;

    public StrategyFactory(NumericValueExtractor extractor) {
        this.extractor = extractor;
    }

    public CheckStrategy createStrategy(MonitoringConfig config) {
        MonitoringMode mode = config.getMode();

        switch (mode) {
            case PHRASES:
                return new PhrasesCheckStrategy();
            case VALUE_BIGGER:
                return new ValueBiggerStrategy(extractor);
            case VALUE_SMALLER:
                return new ValueSmallerStrategy(extractor);
            case SITE_BIGGER_THAN:
                return new SiteBiggerThanStrategy();
            case SITE_SMALLER_THAN:
                return new SiteSmallerThanStrategy();
            case CHECK_VALUE:
            default:
                return new CheckValueStrategy();
        }
    }
}

