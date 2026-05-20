package com.jointpay.common.http;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 JDK {@link HttpClient} 的默认实现。
 */
public final class JdkHttpTransport implements HttpTransport {

    private final HttpClient client;

    public JdkHttpTransport() {
        this(Duration.ofSeconds(30));
    }

    public JdkHttpTransport(Duration timeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    @Override
    public HttpResponse execute(HttpRequest request) {
        try {
            java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(request.getUrl()));
            request.getHeaders().forEach(builder::header);
            if (request.getMethod() == HttpMethod.GET) {
                builder.GET();
            } else {
                String body = request.getBody() == null ? "" : request.getBody();
                builder.POST(java.net.http.HttpRequest.BodyPublishers.ofString(body));
            }
            var response = client.send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
            return new HttpResponse(
                    response.statusCode(),
                    response.body(),
                    toHeaderMap(response.headers().map()));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new JointPayException(ErrorCode.NETWORK_ERROR, "HTTP 请求失败", null, null, e);
        }
    }

    private static Map<String, String> toHeaderMap(Map<String, List<String>> raw) {
        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(",", e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }
}
