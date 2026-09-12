package org.libraryexpress.infrastructure.api.enums;

public enum HttpVerb {

    GET("get"),
    POST("post"),
    PUT("put"),
    PATCH("patch"),
    DELETE("delete");

    private final String verb;

    HttpVerb(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb.toUpperCase();
    }
}
