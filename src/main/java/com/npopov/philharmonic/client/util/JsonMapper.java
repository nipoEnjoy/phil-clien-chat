package com.npopov.philharmonic.client.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Shared Jackson ObjectMapper configured for the Philharmonic API:
 *  - camelCase ↔ camelCase (server returns camelCase JSON)
 *  - JavaTimeModule for LocalDate / LocalDateTime
 *  - unknown properties ignored (forward compatibility)
 */
public final class JsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonMapper() {}

    public static ObjectMapper get() {
        return MAPPER;
    }
}
