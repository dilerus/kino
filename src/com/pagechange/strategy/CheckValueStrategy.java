package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;

public class CheckValueStrategy implements CheckStrategy {

    @Override
    public CheckResult check(String currentPage, MonitoringState state, MonitoringConfig config) {
        if (state.getPreviousPage() == null) {
            return new CheckResult(false);
        }

        boolean changed = !currentPage.equals(state.getPreviousPage());
        return new CheckResult(changed);
    }

    @Override
    public String getSuccessMessage(MonitoringState state, MonitoringConfig config) {
        return "jest zmiana strony";
    }

    @Override
    public String getDefeatMessage(MonitoringState state, MonitoringConfig config, String currentPage) {
        return "Brak zmiany strony. - wielkosc strony: " + currentPage.length();
    }
}

