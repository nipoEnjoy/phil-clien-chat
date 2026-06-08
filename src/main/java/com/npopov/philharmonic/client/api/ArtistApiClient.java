package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.ArtistModel;

import java.util.List;
import java.util.Map;

public class ArtistApiClient extends BaseApiClient {

    private static final ArtistApiClient INSTANCE = new ArtistApiClient();
    public static ArtistApiClient getInstance() { return INSTANCE; }
    private ArtistApiClient() {}

    public List<ArtistModel> findAll() {
        return getList("/api/artists", new TypeReference<>() {});
    }

    public List<ArtistModel> findByGenre(String genre) {
        return getList("/api/artists?genre=" + encode(genre), new TypeReference<>() {});
    }

    public ArtistModel create(Map<String, Object> body) {
        return post("/api/artists", body, ArtistModel.class);
    }

    public ArtistModel update(Long id, Map<String, Object> body) {
        return put("/api/artists/" + id, body, ArtistModel.class);
    }

    public void delete(Long id) {
        delete("/api/artists/" + id);
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
