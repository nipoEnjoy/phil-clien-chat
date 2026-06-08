package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.CompetitionResultModel;
import java.util.List;
import java.util.Map;

public class CompetitionResultApiClient extends BaseApiClient {
    private static final CompetitionResultApiClient INSTANCE = new CompetitionResultApiClient();
    public static CompetitionResultApiClient getInstance() { return INSTANCE; }
    private CompetitionResultApiClient() {}

    public List<CompetitionResultModel> findAll() {
        return getList("/api/competition-results", new TypeReference<>() {});
    }
    public List<CompetitionResultModel> findByCompetition(Long competitionId) {
        return getList("/api/competition-results?competitionId=" + competitionId, new TypeReference<>() {});
    }
    public CompetitionResultModel create(Map<String, Object> body) {
        return post("/api/competition-results", body, CompetitionResultModel.class);
    }
    public CompetitionResultModel update(Long id, Map<String, Object> body) {
        return put("/api/competition-results/" + id, body, CompetitionResultModel.class);
    }
    public void delete(Long id) { delete("/api/competition-results/" + id); }
}
