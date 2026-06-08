package com.npopov.philharmonic.client.api;

import com.npopov.philharmonic.client.util.AppConfig;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Provides a single shared {@link HttpClient} instance.
 */
public final class HttpClientFactory {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(AppConfig.CONNECT_TIMEOUT_SECONDS))
            .build();

    private HttpClientFactory() {}

    public static HttpClient get() {
        return CLIENT;
    }
}
