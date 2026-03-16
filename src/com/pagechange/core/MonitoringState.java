package com.pagechange.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MonitoringState {
    private String currentPage;
    private String previousPage;
    private Float actualValue;
    private long iterationsRemaining;

    public MonitoringState(long totalIterations) {
        this.iterationsRemaining = totalIterations;
    }
}

