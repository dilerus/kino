package com.pagechange.config;

public final class AppConstants {
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final int EMPTY_PAGE_RETRIES = 5;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 5000;
    public static final int SOUND_REPEATS_ON_SUCCESS = 10_000;
    public static final int SOUND_REPEATS_ON_EMAIL = 3;
    public static final int EMAIL_RETRY_LIMIT = 5;
    public static final int EMAIL_RETRY_DELAY_MS = 30_000;
    public static final int EMPTY_PAGE_RETRY_DELAY_MS = 30_000;
    public static final long EXIT_DELAY_SECONDS = 30;
    public static final long SUCCESS_EXIT_DELAY_SECONDS = 3_600;

    public static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    public static final String ACCEPT_ENCODING = "identity";

    public static final String SOUND_RESOURCE_PATH = "/resources/tada.wav";

    // SMTP Configuration
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    public static final String SMTP_USERNAME = "dilerus.robot";
    public static final String SMTP_PASSWORD = "qjbd zxst lotm ajbk";
}

