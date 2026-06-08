package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.GenreModel;

import java.util.List;
import java.util.Map;

public class GenreApiClient extends BaseApiClient {

    private static final GenreApiClient INSTANCE = new GenreApiClient();
    public static GenreApiClient getInstance() { return INSTANCE; }
    private GenreApiClient() {}

    public List<GenreModel> findAll() {
        return getList("/api/genres", new TypeReference<>() {});
    }

    public GenreModel create(Map<String, Object> body) {
        return post("/api/genres", body, GenreModel.class);
    }

    public GenreModel update(Long id, Map<String, Object> body) {
        return put("/api/genres/" + id, body, GenreModel.class);
    }

    public void delete(Long id) {
        delete("/api/genres/" + id);
    }
}