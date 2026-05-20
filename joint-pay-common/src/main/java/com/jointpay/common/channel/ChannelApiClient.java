package com.jointpay.common.channel;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.common.http.HttpContentTypes;
import com.jointpay.common.http.HttpMethod;
import com.jointpay.common.http.HttpRequest;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.http.HttpTransport;
import com.jointpay.common.http.JdkHttpTransport;

import java.util.HashMap;
import java.util.Map;

/**
 * 渠道 HTTP 调用基类。
 */
public class ChannelApiClient {

    private final ChannelConfig config;
    private final HttpTransport httpTransport;

    public ChannelApiClient(ChannelConfig config) {
        this(config, new JdkHttpTransport());
    }

    public ChannelApiClient(ChannelConfig config, HttpTransport httpTransport) {
        this.config = config;
        this.httpTransport = httpTransport;
    }

    public ChannelConfig getConfig() {
        return config;
    }

    public HttpResponse postJson(String path, Object body) {
        return post(path, HttpContentTypes.JSON, com.jointpay.common.json.Jsons.toJson(body));
    }

    public HttpResponse postForm(String path, Map<String, String> form) {
        return post(path, HttpContentTypes.FORM, com.jointpay.common.http.FormEncoder.encode(form));
    }

    public HttpResponse post(String path, String contentType, String body) {
        String baseUrl = resolveGateway();
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + path : baseUrl + path;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        HttpRequest request = HttpRequest.builder(HttpMethod.POST, url)
                .headers(headers)
                .body(body)
                .build();
        HttpResponse response = httpTransport.execute(request);
        if (!response.isSuccessful()) {
            throw new JointPayException(
                    ErrorCode.NETWORK_ERROR,
                    "HTTP " + response.getStatusCode());
        }
        return response;
    }

    private String resolveGateway() {
        String gateway = config.getGatewayUrl();
        if (gateway == null || gateway.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "gatewayUrl 未配置");
        }
        return gateway;
    }
}
