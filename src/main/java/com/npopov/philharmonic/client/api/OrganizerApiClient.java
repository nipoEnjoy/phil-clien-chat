package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.OrganizerModel;
import java.util.List;
import java.util.Map;

public class OrganizerApiClient extends BaseApiClient {
    private static final OrganizerApiClient INSTANCE = new OrganizerApiClient();
    public static OrganizerApiClient getInstance() { return INSTANCE; }
    private OrganizerApiClient() {}

    public List<OrganizerModel> findAll() {
        return getList("/api/organizers", new TypeReference<>() {});
    }
    public OrganizerModel create(Map<String, Object> body) {
        return post("/api/organizers", body, OrganizerModel.class);
    }
    public OrganizerModel update(Long id, Map<String, Object> body) {
        return put("/api/organizers/" + id, body, OrganizerModel.class);
    }
    public void delete(Long id) { delete("/api/organizers/" + id); }
}
