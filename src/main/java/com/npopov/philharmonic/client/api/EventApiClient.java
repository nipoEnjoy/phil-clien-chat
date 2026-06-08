package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.EventModel;

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
        return post("/api/events", body, EventModel.class);
    }

    public EventModel update(Long id, Map<String, Object> body) {
        return put("/api/events/" + id, body, EventModel.class);
    }

    public void delete(Long id) {
        delete("/api/events/" + id);
    }
}
