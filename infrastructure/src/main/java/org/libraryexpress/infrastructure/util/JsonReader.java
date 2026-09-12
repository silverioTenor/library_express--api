package org.libraryexpress.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.libraryexpress.infrastructure.config.JacksonConfig;

import java.io.IOException;
import java.io.InputStream;

public final class JsonReader {

    private JsonReader() {}

    public static <T> T read(String json, Class<T> targetType) {
        try {
            return JacksonConfig.mapper().readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON for type " + targetType.getSimpleName(), e);
        }
    }

    public static <T> T read(InputStream src, Class<T> targetType) {
        try {
            return JacksonConfig.mapper().readValue(src, targetType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON for type " + targetType.getSimpleName(), e);
        }
    }
}
