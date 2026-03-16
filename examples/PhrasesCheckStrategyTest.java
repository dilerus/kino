package com.pagechange.strategy;

import com.pagechange.config.MonitoringConfig;
import com.pagechange.core.MonitoringState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Przykład testów jednostkowych dla PhrasesCheckStrategy
 *
 * Aby uruchomić, dodaj do lib/:
 * - junit-jupiter-api-5.10.0.jar
 * - junit-jupiter-engine-5.10.0.jar
 * - mockito-core-5.5.0.jar
 */
public class PhrasesCheckStrategyTest {

    @Test
    public void testCheckSuccess_phraseFound() {
        // Given
        PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
        MonitoringConfig config = MonitoringConfig.builder()
                .addPhrase("testphrase")
                .negation(false)
                .build();
        MonitoringState state = new MonitoringState(10);
        String page = "this is a page with testphrase inside";

        // When
        CheckResult result = strategy.check(page, state, config);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("testphrase", result.getMatchedPhrase());
    }

    @Test
    public void testCheckFail_phraseNotFound() {
        // Given
        PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
        MonitoringConfig config = MonitoringConfig.builder()
                .addPhrase("missingphrase")
                .negation(false)
                .build();
        MonitoringState state = new MonitoringState(10);
        String page = "this is a page without the phrase";

        // When
        CheckResult result = strategy.check(page, state, config);

        // Then
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCheckSuccess_negation_phraseNotFound() {
        // Given
        PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
        MonitoringConfig config = MonitoringConfig.builder()
                .addPhrase("unwantedphrase")
                .negation(true)
                .build();
        MonitoringState state = new MonitoringState(10);
        String page = "this is a clean page";

        // When
        CheckResult result = strategy.check(page, state, config);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("unwantedphrase", result.getMatchedPhrase());
    }

    @Test
    public void testGetSuccessMessageForPhrase() {
        // Given
        PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
        MonitoringConfig config = MonitoringConfig.builder()
                .negation(false)
                .build();

        // When
        String message = strategy.getSuccessMessageForPhrase("testphrase", config);

        // Then
        assertEquals("znaleziono fraze: testphrase", message);
    }

    @Test
    public void testGetSuccessMessageForPhrase_negation() {
        // Given
        PhrasesCheckStrategy strategy = new PhrasesCheckStrategy();
        MonitoringConfig config = MonitoringConfig.builder()
                .negation(true)
                .build();

        // When
        String message = strategy.getSuccessMessageForPhrase("testphrase", config);

        // Then
        assertEquals("nie znaleziono frazy: testphrase", message);
    }
}

