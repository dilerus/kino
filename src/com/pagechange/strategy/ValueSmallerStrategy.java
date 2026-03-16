package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;
import com.pagechange.util.NumericValueExtractor;

public class ValueSmallerStrategy implements CheckStrategy {
    private final NumericValueExtractor extractor;

    public ValueSmallerStrategy(NumericValueExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public CheckResult check(String currentPage, MonitoringState state, MonitoringConfig config) {
        Float value = extractor.extractValue(currentPage, config.getPreValue());

        if (value == null) {
            System.out.println("Fragment '" + config.getPreValue() + "' nie zostal znaleziony na stronie.");
            return new CheckResult(false);
        }

        state.setActualValue(value);
        boolean success = value.compareTo(config.getThresholdValue()) < 0;
        return new CheckResult(success);
    }

    @Override
    public String getSuccessMessage(MonitoringState state, MonitoringConfig config) {
        return "Znaleziona wartosc: " + state.getActualValue() +
               " jest mniejsza niz ustawiona wartosc progowa: " + config.getThresholdValue();
    }

    @Override
    public String getDefeatMessage(MonitoringState state, MonitoringConfig config, String currentPage) {
        return "Znaleziona wartosc: '" + state.getActualValue() +
               "', nie jest mniejsza niz ustawiona wartosc progowa: '" + config.getThresholdValue() +
               "' - wielkosc strony: " + currentPage.length();
    }
}

