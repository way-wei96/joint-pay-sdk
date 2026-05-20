package com.jointpay.common.http;

import java.util.Map;

public final class HttpResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
