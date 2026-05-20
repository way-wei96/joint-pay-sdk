package com.jointpay.common.http;

import java.util.Map;

public final class HttpRequest {

    private final HttpMethod method;
    private final String url;
    private final String body;
    private final Map<String, String> headers;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.body = builder.body;
        this.headers = builder.headers == null ? Map.of() : Map.copyOf(builder.headers);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static Builder builder(HttpMethod method, String url) {
        return new Builder(method, url);
    }

    public static final class Builder {
        private final HttpMethod method;
        private final String url;
        private String body;
        private Map<String, String> headers;

        private Builder(HttpMethod method, String url) {
            this.method = method;
            this.url = url;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
