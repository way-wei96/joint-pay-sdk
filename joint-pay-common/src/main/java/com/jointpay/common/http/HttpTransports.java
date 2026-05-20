package com.jointpay.common.http;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 全局 HTTP 传输层入口，默认 {@link JdkHttpTransport}；可替换为带代理、日志或 Mock 的实现。
 */
public final class HttpTransports {

    private static volatile Supplier<HttpTransport> supplier = () -> new JdkHttpTransport(Duration.ofSeconds(30));

    private HttpTransports() {
    }

    public static void use(Supplier<HttpTransport> transportSupplier) {
        supplier = transportSupplier == null
                ? () -> new JdkHttpTransport(Duration.ofSeconds(30))
                : transportSupplier;
    }

    public static void use(HttpTransport transport) {
        supplier = () -> transport;
    }

    public static HttpTransport create() {
        return supplier.get();
    }
}
