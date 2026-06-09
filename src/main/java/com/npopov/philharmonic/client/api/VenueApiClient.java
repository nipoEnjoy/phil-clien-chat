package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.VenueModel;

import java.util.List;
import java.util.Map;

public class VenueApiClient extends BaseApiClient {

    private static final VenueApiClient INSTANCE = new VenueApiClient();
    public static VenueApiClient getInstance() { return INSTANCE; }
    private VenueApiClient() {}

    public List<VenueModel> findAll() {
        return getList("/api/venues", new TypeReference<>() {});
    }

    public List<VenueModel> findByType(String type) {
        return getList("/api/venues?type=" + type, new TypeReference<>() {});
    }

    public VenueModel create(Map<String, Object> body) {
        System.out.println("Sending data /api/venues: " + body);
        return post("/api/venues", body, VenueModel.class);
    }

    public VenueModel update(Long id, Map<String, Object> body) {
        System.out.println("Sending data /api/venues: " + body);
        return put("/api/venues/" + id, body, VenueModel.class);
    }

    public void delete(Long id) {
        delete("/api/venues/" + id);
    }
}
