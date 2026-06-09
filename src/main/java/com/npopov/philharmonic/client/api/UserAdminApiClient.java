package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.UserModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserAdminApiClient extends BaseApiClient {
    private static final UserAdminApiClient INSTANCE = new UserAdminApiClient();
    public static UserAdminApiClient getInstance() { return INSTANCE; }
    private UserAdminApiClient() {}

    public List<UserModel> findAll() {
        return getList("/api/users", new TypeReference<>() {});
    }

    public UserModel create(Map<String, Object> body) {
        return post("/api/users", body, UserModel.class);
    }

    public UserModel update(Long id, Map<String, Object> body) {
        return put("/api/users/" + id, body, UserModel.class);
    }

    public void delete(Long id) {
        delete("/api/users/" + id);
    }

    public UserModel assignRoles(Long userId, Set<Long> roleIds) {
        return put("/api/users/" + userId + "/roles", Map.of("roleIds", roleIds), UserModel.class);
    }
}