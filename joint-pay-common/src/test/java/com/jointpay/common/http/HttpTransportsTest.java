package com.jointpay.common.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTransportsTest {

    @AfterEach
    void reset() {
        HttpTransports.use(() -> new JdkHttpTransport(Duration.ofSeconds(30)));
    }

    @Test
    void useCustomTransport() {
        HttpTransport custom = (request) -> new HttpResponse(200, "ok", java.util.Map.of());
        AtomicReference<HttpTransport> ref = new AtomicReference<>();
        HttpTransports.use(() -> {
            ref.set(custom);
            return custom;
        });
        assertSame(custom, HttpTransports.create());
        assertTrue(ref.get() instanceof HttpTransport);
    }
}
