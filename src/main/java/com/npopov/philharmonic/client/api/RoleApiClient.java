package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.RoleModel;

import java.util.List;
import java.util.Map;

public class RoleApiClient extends BaseApiClient {
    private static final RoleApiClient INSTANCE = new RoleApiClient();
    public static RoleApiClient getInstance() { return INSTANCE; }
    private RoleApiClient() {}

    public List<RoleModel> findAll() {
        return getList("/api/roles", new TypeReference<>() {});
    }

    public RoleModel create(Map<String, Object> body) {
        return post("/api/roles", body, RoleModel.class);
    }

    public RoleModel update(Long id, Map<String, Object> body) {
        return put("/api/roles/" + id, body, RoleModel.class);
    }

    public void delete(Long id) {
        delete("/api/roles/" + id);
    }
}