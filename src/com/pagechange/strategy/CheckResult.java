package com.pagechange.strategy;

public class CheckResult {
    private final boolean success;
    private final String matchedPhrase;

    public CheckResult(boolean success, String matchedPhrase) {
        this.success = success;
        this.matchedPhrase = matchedPhrase;
    }

    public CheckResult(boolean success) {
        this(success, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMatchedPhrase() {
        return matchedPhrase;
    }
}

