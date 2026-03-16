package com.pagechange.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericValueExtractor {

    public Float extractValue(String page, String prefix) {
        int position = page.indexOf(prefix);
        if (position == -1) {
            return null;
        }

        int endPos = Math.min(position + prefix.length() + 20, page.length());
        String text = page.substring(position + prefix.length(), endPos);

        Pattern pattern = Pattern.compile("[0-9,.]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String numberStr = matcher.group();
            return parseNumber(numberStr);
        }

        return null;
    }

    public Long extractLongValue(String text, String prefix) {
        if (!text.contains(prefix)) {
            return null;
        }

        int startPos = text.indexOf(prefix) + prefix.length();
        int endPos = Math.min(startPos + 20, text.length());
        String textAfterPrefix = text.substring(startPos, endPos);

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(textAfterPrefix);

        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        }

        return null;
    }

    private float parseNumber(String numberStr) {
        int commaCounter = countChar(numberStr, ',');
        int dotCounter = countChar(numberStr, '.');

        if (commaCounter > dotCounter) {
            numberStr = numberStr.replaceAll(",", "");
        } else if (commaCounter < dotCounter) {
            numberStr = numberStr.replaceAll("\\.", "");
        } else if (commaCounter > 0) {
            if (numberStr.indexOf(",") > numberStr.indexOf(".")) {
                numberStr = numberStr.replaceAll("\\.", "");
            } else {
                numberStr = numberStr.replaceAll(",", "");
            }
        }

        return Float.parseFloat(numberStr);
    }

    private int countChar(String text, char ch) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
}

