package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.ImpresarioModel;
import java.util.List;
import java.util.Map;

public class ImpresarioApiClient extends BaseApiClient {
    private static final ImpresarioApiClient INSTANCE = new ImpresarioApiClient();
    public static ImpresarioApiClient getInstance() { return INSTANCE; }
    private ImpresarioApiClient() {}

    public List<ImpresarioModel> findAll() {
        return getList("/api/impresarios", new TypeReference<>() {});
    }
    public ImpresarioModel create(Map<String, Object> body) {
        return post("/api/impresarios", body, ImpresarioModel.class);
    }
    public ImpresarioModel update(Long id, Map<String, Object> body) {
        return put("/api/impresarios/" + id, body, ImpresarioModel.class);
    }
    public void delete(Long id) { delete("/api/impresarios/" + id); }
}
