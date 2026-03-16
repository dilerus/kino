package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;

public class SiteSmallerThanStrategy implements CheckStrategy {

    @Override
    public CheckResult check(String currentPage, MonitoringState state, MonitoringConfig config) {
        boolean success = currentPage.length() < config.getSiteSize();
        return new CheckResult(success);
    }

    @Override
    public String getSuccessMessage(MonitoringState state, MonitoringConfig config) {
        return "Wielkosc strony: " + state.getCurrentPage().length() +
               " jest mniejsza niz ustawiona wartosc progowa: " + config.getSiteSize();
    }

    @Override
    public String getDefeatMessage(MonitoringState state, MonitoringConfig config, String currentPage) {
        return "Wielkosc strony: '" + currentPage.length() +
               "', nie jest mniejsza niz ustawiona wartosc progowa: '" + config.getSiteSize() + "'";
    }
}

