package com.npopov.philharmonic.client.util;

/**
 * Centralized application configuration.
 * Base URL can be overridden via system property: -Dapi.baseUrl=http://...
 */
public final class AppConfig {

    public static final String BASE_URL =
            System.getProperty("api.baseUrl", "http://localhost:8080");

    public static final int CONNECT_TIMEOUT_SECONDS = 5;
    public static final int REQUEST_TIMEOUT_SECONDS = 15;

    // Window sizing
    public static final double LOGIN_WIDTH  = 420;
    public static final double LOGIN_HEIGHT = 520;
    public static final double MAIN_WIDTH   = 1280;
    public static final double MAIN_HEIGHT  = 800;

    private AppConfig() {}
}
