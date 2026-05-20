package com.jointpay.example;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.payment.PrepayRequest;
import com.jointpay.common.http.HttpRequest;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.http.HttpTransports;
import com.jointpay.core.PayClientFactory;

import java.util.Map;

/**
 * 单测/本地调试：用 Mock HTTP 避免真实请求网关（需引入对应渠道模块）。
 */
public final class MockHttpDemo {

    private MockHttpDemo() {
    }

    public static void main(String[] args) {
        HttpTransports.use(request -> {
            System.out.println("Mock HTTP -> " + request.getUrl());
            return new HttpResponse(200, "{\"code\":200,\"data\":{}}", Map.of());
        });

        try {
            var client = PayClientFactory.create(ChannelConfig.builder(PayChannel.JOINPAY)
                    .merchantId("M1")
                    .apiSecret("secret")
                    .appId("app")
                    .gatewayUrl("https://mock.local")
                    .build());
            client.payment().prepay(PrepayRequest.builder()
                    .outTradeNo("MOCK001")
                    .amountCent(100L)
                    .subject("mock")
                    .extras(Map.of("frpCode", "ALIPAY_H5"))
                    .build());
        } catch (JointPayException e) {
            if (e.getErrorCode() != ErrorCode.CHANNEL_ERROR) {
                throw e;
            }
            System.out.println("预期可能因 Mock 响应体格式不完整而 CHANNEL_ERROR: " + e.getMessage());
        }
    }
}
