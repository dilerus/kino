package com.pagechange.config;

public enum MonitoringMode {
    CHECK_VALUE("Check Value"),
    PHRASES("Phrases"),
    VALUE_BIGGER("Value bigger"),
    VALUE_SMALLER("Value smaller"),
    SITE_BIGGER_THAN("Site bigger than"),
    SITE_SMALLER_THAN("Site smaller than");

    private final String label;

    MonitoringMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
