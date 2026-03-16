package com.pagechange.util;

}
    }
        assertEquals("", result);
        // Then

        String result = normalizer.normalize(null);
        // When

        StringNormalizer normalizer = new StringNormalizer();
        // Given
    public void testNormalize_nullInput() {
    @Test

    }
        assertEquals("hello", result);
        // Then

        String result = normalizer.normalize(input);
        // When

        String input = "\"hello\"";
        StringNormalizer normalizer = new StringNormalizer();
        // Given
    public void testNormalize_removesQuotes() {
    @Test

    }
        assertEquals("zazolcgeslajaźn", result);
        // Then

        String result = normalizer.normalize(input);
        // When

        String input = "zażółć gęślą jaźń";
        StringNormalizer normalizer = new StringNormalizer();
        // Given
    public void testNormalize_removesPolishCharacters() {
    @Test

    }
        assertEquals("helloworld", result);
        // Then

        String result = normalizer.normalize(input);
        // When

        String input = "HELLO World";
        StringNormalizer normalizer = new StringNormalizer();
        // Given
    public void testNormalize_toLowerCase() {
    @Test

    }
        assertEquals("helloworld", result);
        // Then

        String result = normalizer.normalize(input);
        // When

        String input = "hello   world";
        StringNormalizer normalizer = new StringNormalizer();
        // Given
    public void testNormalize_removesSpaces() {
    @Test

public class StringNormalizerTest {
 */
 * Testy są proste i szybkie dzięki brakowi zależności
 *
 * Przykład testów dla StringNormalizer
/**

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

