package com.pagechange.config;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonitoringConfig {
    private final URL url;
    private final long interval;
    private final long finish;
    private final List<String> emails;
    private final boolean sound;
    private final boolean negation;
    private final DayOfWeek day;
    private final LocalTime hour;
    private final LocalDate date;
    private final boolean debug;
    private final MonitoringMode mode;

    // Mode-specific parameters
    private final List<String> phrases;
    private final String preValue;
    private final Float thresholdValue;
    private final long siteSize;
    private final String prefixIncrementation;

    private MonitoringConfig(Builder builder) {
        this.url = builder.url;
        this.interval = builder.interval;
        this.finish = builder.finish;
        this.emails = Collections.unmodifiableList(new ArrayList<>(builder.emails));
        this.sound = builder.sound;
        this.negation = builder.negation;
        this.day = builder.day;
        this.hour = builder.hour;
        this.date = builder.date;
        this.debug = builder.debug;
        this.mode = builder.mode;
        this.phrases = Collections.unmodifiableList(new ArrayList<>(builder.phrases));
        this.preValue = builder.preValue;
        this.thresholdValue = builder.thresholdValue;
        this.siteSize = builder.siteSize;
        this.prefixIncrementation = builder.prefixIncrementation;
    }

    public URL getUrl() {
        return url;
    }

    public long getInterval() {
        return interval;
    }

    public long getFinish() {
        return finish;
    }

    public List<String> getEmails() {
        return emails;
    }

    public boolean isSound() {
        return sound;
    }

    public boolean isNegation() {
        return negation;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getHour() {
        return hour;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isDebug() {
        return debug;
    }

    public MonitoringMode getMode() {
        return mode;
    }

    public List<String> getPhrases() {
        return phrases;
    }

    public String getPreValue() {
        return preValue;
    }

    public Float getThresholdValue() {
        return thresholdValue;
    }

    public long getSiteSize() {
        return siteSize;
    }

    public String getPrefixIncrementation() {
        return prefixIncrementation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private URL url;
        private long interval = 10L;
        private long finish = 1_000_000L;
        private List<String> emails = new ArrayList<>();
        private boolean sound = false;
        private boolean negation = false;
        private DayOfWeek day;
        private LocalTime hour;
        private LocalDate date;
        private boolean debug = false;
        private MonitoringMode mode = MonitoringMode.CHECK_VALUE;
        private List<String> phrases = new ArrayList<>();
        private String preValue;
        private Float thresholdValue;
        private long siteSize;
        private String prefixIncrementation;

        public Builder url(URL url) {
            this.url = url;
            return this;
        }

        public Builder interval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder finish(long finish) {
            this.finish = finish;
            return this;
        }

        public Builder emails(List<String> emails) {
            this.emails = emails;
            return this;
        }

        public Builder addEmail(String email) {
            this.emails.add(email);
            return this;
        }

        public Builder sound(boolean sound) {
            this.sound = sound;
            return this;
        }

        public Builder negation(boolean negation) {
            this.negation = negation;
            return this;
        }

        public Builder day(DayOfWeek day) {
            this.day = day;
            return this;
        }

        public Builder hour(LocalTime hour) {
            this.hour = hour;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder mode(MonitoringMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder phrases(List<String> phrases) {
            this.phrases = phrases;
            return this;
        }

        public Builder addPhrase(String phrase) {
            this.phrases.add(phrase);
            return this;
        }

        public Builder preValue(String preValue) {
            this.preValue = preValue;
            return this;
        }

        public Builder thresholdValue(Float thresholdValue) {
            this.thresholdValue = thresholdValue;
            return this;
        }

        public Builder siteSize(long siteSize) {
            this.siteSize = siteSize;
            return this;
        }

        public Builder prefixIncrementation(String prefixIncrementation) {
            this.prefixIncrementation = prefixIncrementation;
            return this;
        }

        public MonitoringConfig build() {
            return new MonitoringConfig(this);
        }
    }
}

