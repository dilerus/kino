package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;

public interface CheckStrategy {
    CheckResult check(String currentPage, MonitoringState state, MonitoringConfig config);
    String getSuccessMessage(MonitoringState state, MonitoringConfig config);
    String getDefeatMessage(MonitoringState state, MonitoringConfig config, String currentPage);
}

