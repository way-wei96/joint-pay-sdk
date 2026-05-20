package com.jointpay.api.notify;

import java.util.Map;

/**
 * 渠道回调原始入参（表单、JSON、XML 等格式由实现层解析）。
 */
public final class NotifyRawRequest {

    private final String body;
    private final Map<String, String> params;
    private final Map<String, String> headers;

    private NotifyRawRequest(Builder builder) {
        this.body = builder.body;
        this.params = builder.params == null ? Map.of() : Map.copyOf(builder.params);
        this.headers = builder.headers == null ? Map.of() : Map.copyOf(builder.headers);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String body;
        private Map<String, String> params;
        private Map<String, String> headers;

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public NotifyRawRequest build() {
            return new NotifyRawRequest(this);
        }
    }
}
