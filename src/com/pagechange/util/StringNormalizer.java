package com.pagechange.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringNormalizer {
    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        input = input.toLowerCase().trim().replaceAll("\\s", "").replaceAll("\"", "");
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalizedString)
                .replaceAll("");
    }
}

