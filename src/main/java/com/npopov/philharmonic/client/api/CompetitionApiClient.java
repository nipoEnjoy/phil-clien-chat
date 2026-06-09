package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.CompetitionModel;
import java.util.List;
import java.util.Map;

public class CompetitionApiClient extends BaseApiClient {
    private static final String PATH = "/api/competitions";
//    private static final String PATH = "/api/events?eventType=" + "competition";

    private static final CompetitionApiClient INSTANCE = new CompetitionApiClient();
    public static CompetitionApiClient getInstance() { return INSTANCE; }
    private CompetitionApiClient() {}

    public List<CompetitionModel> findAll() {
        return getList(PATH, new TypeReference<>() {});
    }

    public CompetitionModel create(Map<String, Object> body) {
        return post(PATH, body, CompetitionModel.class);
    }
    public CompetitionModel update(Long id, Map<String, Object> body) {
        return put(PATH + id, body, CompetitionModel.class);
    }
    public void delete(Long id) { delete(PATH + id); }
}
