package com.pagechange.util;

public class AnsiColorFormatter {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String PURPLE = "\u001B[35m";
    private static final String RED_BACKGROUND = "\u001B[01;41m";

    public String formatError(String message) {
        return RED + message + RESET;
    }

    public String formatSuccess(String message) {
        return RED_BACKGROUND + message + RESET;
    }

    public String formatInfo(String message) {
        return GREEN + message + RESET;
    }

    public String formatHighlight(String message) {
        return PURPLE + message + RESET;
    }

    public String highlight(String message) {
        return PURPLE + message + RESET;
    }
}

