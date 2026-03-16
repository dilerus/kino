package com.pagechange.http;

import com.pagechange.config.AppConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WebPageFetcher {
    private static final int MAX_REDIRECTS = 5;

    public String fetchPage(URL url) {
        return fetchPageWithRedirects(url, 0);
    }

    private String fetchPageWithRedirects(URL url, int redirectCount) {
        if (redirectCount >= MAX_REDIRECTS) {
            System.out.println("Przekroczono maksymalna liczbe przekierowan: " + MAX_REDIRECTS);
            return "";
        }

        StringBuilder content = new StringBuilder();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Set headers
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("User-Agent", AppConstants.USER_AGENT);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", AppConstants.ACCEPT_ENCODING);

            conn.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT);
            conn.setReadTimeout(AppConstants.READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);

            int responseCode = conn.getResponseCode();

            if (responseCode >= 300 && responseCode < 400) {
                String redirectUrl = conn.getHeaderField("Location");
                try {
                    return fetchPageWithRedirects(new URI(redirectUrl).toURL(), redirectCount + 1);
                } catch (URISyntaxException e) {
                    System.out.println("Nieprawidlowy URL przekierowania: " + redirectUrl);
                    return "";
                }
            } else if (responseCode != 200) {
                System.out.println("Blad HTTP: " + responseCode);
                return "";
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

        } catch (IOException e) {
            System.out.println("Nie udalo sie polaczyc z podanym adresem: " + e.getMessage());
            return "";
        }

        return content.toString();
    }
}

