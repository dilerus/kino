package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;

import java.util.List;

public class PhrasesCheckStrategy implements CheckStrategy {

    @Override
    public CheckResult check(String currentPage, MonitoringState state, MonitoringConfig config) {
        List<String> phrases = config.getPhrases();

        for (String phrase : phrases) {
            boolean containsPhrase = currentPage.contains(phrase);
            boolean success = config.isNegation() ? !containsPhrase : containsPhrase;

            if (success) {
                return new CheckResult(true, phrase);
            }
        }

        return new CheckResult(false);
    }

    @Override
    public String getSuccessMessage(MonitoringState state, MonitoringConfig config) {
        return ""; // Will be filled by matched phrase
    }

    @Override
    public String getDefeatMessage(MonitoringState state, MonitoringConfig config, String currentPage) {
        return ""; // Handled separately for each phrase
    }

    public String getDefeatMessageForPhrase(String phrase, MonitoringConfig config, String currentPage) {
        String result = "szukam ";
        if (config.isNegation()) {
            result += "braku ";
        }
        result += "tekstu: " + phrase + "... - wielkosc strony: " + currentPage.length();
        return result;
    }

    public String getSuccessMessageForPhrase(String phrase, MonitoringConfig config) {
        if (config.isNegation()) {
            return "nie znaleziono frazy: " + phrase;
        } else {
            return "znaleziono fraze: " + phrase;
        }
    }
}

