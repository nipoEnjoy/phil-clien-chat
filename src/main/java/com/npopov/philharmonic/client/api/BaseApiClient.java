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

//    private HttpRequest.BodyPublisher jsonBody(Object obj) {
//        try {
//            return HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(obj));
//        } catch (Exception e) {
//            throw new ApiException("Failed to serialize request body", e);
//        }
//    }

    private String execute(HttpRequest request) {
        logRequest(request);
        try {
            HttpResponse<String> response = HttpClientFactory.get()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String respBody = response.body();

            // логировать статус и укороченный ответ
            String preview = respBody == null ? "" :
                    (respBody.length() <= 2000 ? respBody : respBody.substring(0, 2000) + "...(truncated)");
            System.out.printf("[API RESPONSE] %s %s -> %d, body length=%d%n%s%n",
                    request.method(), request.uri(), status,
                    respBody == null ? 0 : respBody.length(), preview);

            if (status >= 200 && status < 300) {
                return respBody;
            }

            throw new ApiException(status,
                    "Server error " + status + ": " + respBody);

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

    // ── Logging ─────────────────────────────────────────────────────────────

    private static final ThreadLocal<String> requestBodyTL = new ThreadLocal<>();

    private HttpRequest.BodyPublisher jsonBody(Object obj) {
        try {
            String json = mapper.writeValueAsString(obj);
            // сохраняем в ThreadLocal чтобы log() мог прочитать
            requestBodyTL.set(json);
            return HttpRequest.BodyPublishers.ofString(json);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize request body", e);
        }
    }

    private void logRequest(HttpRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[API REQUEST] ")
                    .append(request.method())
                    .append(" ")
                    .append(request.uri())
                    .append("\n");

            sb.append("Timeout: ").append(request.timeout().orElse(timeout)).append("\n");

            // Headers — redact Authorization
            sb.append("Headers:\n");
            request.headers().map().forEach((k, v) -> {
                String value = String.join(", ", v);
                if ("authorization".equalsIgnoreCase(k)) {
                    value = "[REDACTED]";
                }
                sb.append("  ").append(k).append(": ").append(value).append("\n");
            });

            // Body: print saved JSON (if any), truncated to e.g. 2000 chars
            String body = requestBodyTL.get();
            if (body != null) {
                int max = 2000;
                String preview = body.length() <= max ? body : body.substring(0, max) + "...(truncated)";
                sb.append("Body (json): length=").append(body.length()).append("\n").append(preview).append("\n");
            } else {
                sb.append("Body: none or not-captured\n");
            }

            System.out.println(sb.toString());
        } catch (Exception e) {
            System.out.println("[API REQUEST] (failed to log request: " + e.getMessage() + ")");
        } finally {
            requestBodyTL.remove();
        }
    }
}