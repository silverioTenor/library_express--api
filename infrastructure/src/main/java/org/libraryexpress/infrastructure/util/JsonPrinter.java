package org.libraryexpress.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import org.libraryexpress.infrastructure.config.JacksonConfig;

public final class JsonPrinter {

    private JsonPrinter() {}

    public static String print(Object obj) {
        try {
            return JacksonConfig.prettyWriter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    public static byte[] printBytes(Object obj) {
        try {
            return JacksonConfig.prettyWriter().writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            return obj.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
