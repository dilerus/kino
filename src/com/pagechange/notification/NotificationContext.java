package com.pagechange.notification;

import com.pagechange.config.MonitoringConfig;

public class NotificationContext {
    private final MonitoringConfig config;
    private final String phrase;
    private final Float actualValue;

    public NotificationContext(MonitoringConfig config, String phrase, Float actualValue) {
        this.config = config;
        this.phrase = phrase;
        this.actualValue = actualValue;
    }

    public MonitoringConfig getConfig() {
        return config;
    }

    public String getPhrase() {
        return phrase;
    }

    public Float getActualValue() {
        return actualValue;
    }
}

