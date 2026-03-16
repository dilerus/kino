package com.pagechange.validation;

import com.pagechange.config.AppConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
    private final Pattern pattern;

    public EmailValidator() {
        this.pattern = Pattern.compile(AppConstants.EMAIL_REGEX);
    }

    public boolean isValid(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

