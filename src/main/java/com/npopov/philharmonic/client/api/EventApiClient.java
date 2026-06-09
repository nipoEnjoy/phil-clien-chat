package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.EventModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class EventApiClient extends BaseApiClient {

    private static final EventApiClient INSTANCE = new EventApiClient();
    public static EventApiClient getInstance() { return INSTANCE; }
    private EventApiClient() {}

    public List<EventModel> findAll() {
        return getList("/api/events", new TypeReference<>() {});
    }

    public List<EventModel> findByVenue(Long venueId) {
        return getList("/api/events?venueId=" + venueId, new TypeReference<>() {});
    }

    public List<EventModel> findByOrganizer(Long organizerId) {
        return getList("/api/events?organizerId=" + organizerId, new TypeReference<>() {});
    }

    public EventModel create(Map<String, Object> body) {
        System.out.println("Sending data: /api/events " + body);
        return post("/api/events", body, EventModel.class);
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    public List<EventModel> findByPeriod(LocalDateTime start, LocalDateTime end) {
        StringBuilder path = new StringBuilder("/api/events?");
        if (start != null) {
            path.append("start=").append(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("&");
        }
        if (end != null) {
            path.append("end=").append(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("&");
        }
        // удалить последний '&' если есть
        if (path.charAt(path.length() - 1) == '&') {
            path.setLength(path.length() - 1);
        }
        return getList(path.toString(), new TypeReference<>() {});
    }

    public EventModel update(Long id, Map<String, Object> body) {
        System.out.println("Sending data /api/events/: " + body);
        return put("/api/events/" + id, body, EventModel.class);
    }

    public void delete(Long id) {
        delete("/api/events/" + id);
    }
}
