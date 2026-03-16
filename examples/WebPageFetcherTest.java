package com.pagechange.http;

import com.pagechange.http.WebPageFetcher;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Przykład testu integracyjnego dla WebPageFetcher
 *
 * Pokazuje jak łatwo jest testować kod po refaktoryzacji
 */
public class WebPageFetcherTest {

    @Test
    public void testFetchPage_success() throws Exception {
        // Given
        WebPageFetcher fetcher = new WebPageFetcher();
        URL url = new URL("http://example.com");

        // When
        String content = fetcher.fetchPage(url);

        // Then
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    public void testFetchPage_invalidUrl() throws Exception {
        // Given
        WebPageFetcher fetcher = new WebPageFetcher();
        URL url = new URL("http://this-domain-definitely-does-not-exist-12345.com");

        // When
        String content = fetcher.fetchPage(url);

        // Then
        // Powinno zwrócić pusty string przy błędzie
        assertEquals("", content);
    }
}

