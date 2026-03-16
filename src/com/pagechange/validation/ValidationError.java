package com.pagechange.validation;

public class ValidationError {
    private final String message;
    private final boolean fatal;

    public ValidationError(String message, boolean fatal) {
        this.message = message;
        this.fatal = fatal;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFatal() {
        return fatal;
    }

    @Override
    public String toString() {
        return message;
    }
}

