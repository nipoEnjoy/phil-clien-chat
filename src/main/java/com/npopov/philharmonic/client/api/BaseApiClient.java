package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npopov.philharmonic.client.session.SessionManager;
import com.npopov.philharmonic.client.util.AppConfig;
import com.npopov.philharmonic.client.util.JsonMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Base class for all API clients.
 * Provides get / post / put / delete helpers that:
 *  - attach the JWT Bearer header automatically
 *  - throw {@link ApiException} on non-2xx responses
 *  - deserialize the JSON body via Jackson
 */
public abstract class BaseApiClient {

    protected final ObjectMapper mapper = JsonMapper.get();
    private final Duration timeout = Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS);

    // ── GET ──────────────────────────────────────────────────────────────────

    protected <T> T get(String path, Class<T> type) {
        HttpRequest request = authorizedBuilder(path).GET().build();
        String body = execute(request);
        return deserialize(body, type);
    }

    protected <T> List<T> getList(String path, TypeReference<List<T>> ref) {
        HttpRequest request = authorizedBuilder(path).GET().build();
        String body = execute(request);
        return deserializeList(body, ref);
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    protected <T> T post(String path, Object payload, Class<T> type) {
        HttpRequest request = authorizedBuilder(path)
                .POST(jsonBody(payload))
                .header("Content-Type", "application/json")
                .build();
        String body = execute(request);
        return deserialize(body, type);
    }

    /** POST without auth — used for /auth/login */
    protected <T> T postAnon(String path, Object payload, Class<T> type) {
        HttpRequest request = anonBuilder(path)
                .POST(jsonBody(payload))
                .header("Content-Type", "application/json")
                .build();
        String body = execute(request);
        return deserialize(body, type);
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    protected <T> T put(String path, Object payload, Class<T> type) {
        HttpRequest request = authorizedBuilder(path)
                .PUT(jsonBody(payload))
                .header("Content-Type", "application/json")
                .build();
        String body = execute(request);
        return deserialize(body, type);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    protected void delete(String path) {
        HttpRequest request = authorizedBuilder(path)
                .DELETE()
                .build();
        execute(request);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private HttpRequest.Builder authorizedBuilder(String path) {
        return anonBuilder(path)
                .header("Authorization", SessionManager.getInstance().bearerHeader());
    }

    private HttpRequest.Builder anonBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_URL + path))
                .timeout(timeout)
                .header("Accept", "application/json");
    }

    private HttpRequest.BodyPublisher jsonBody(Object obj) {
        try {
            return HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(obj));
        } catch (Exception e) {
            throw new ApiException("Failed to serialize request body", e);
        }
    }

    private String execute(HttpRequest request) {
        try {
            HttpResponse<String> response = HttpClientFactory.get()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            throw new ApiException(response.statusCode(),
                    "Server error " + response.statusCode() + ": " + response.body());

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Network error: " + ex.getMessage(), ex);
        }
    }

    private <T> T deserialize(String body, Class<T> type) {
        if (body == null || body.isBlank()) return null;
        try {
            return mapper.readValue(body, type);
        } catch (Exception e) {
            throw new ApiException("Failed to parse response: " + e.getMessage(), e);
        }
    }

    private <T> List<T> deserializeList(String body, TypeReference<List<T>> ref) {
        if (body == null || body.isBlank()) return List.of();
        try {
            return mapper.readValue(body, ref);
        } catch (Exception e) {
            throw new ApiException("Failed to parse response list: " + e.getMessage(), e);
        }
    }
}
