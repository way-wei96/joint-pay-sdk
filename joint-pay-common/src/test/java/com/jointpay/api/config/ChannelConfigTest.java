package com.jointpay.api.config;

import com.jointpay.api.PayChannel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelConfigTest {

    @Test
    void builderCopiesSourceAndOverridesGateway() {
        ChannelConfig source = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("M1")
                .appId("A1")
                .apiSecret("SECRET")
                .gatewayUrl("https://trade.example.com")
                .extras(Map.of("k", "v"))
                .build();

        ChannelConfig copy = ChannelConfig.builder(source)
                .gatewayUrl("https://openapi.example.com")
                .build();

        assertEquals(PayChannel.JOINPAY, copy.getChannel());
        assertEquals("M1", copy.getMerchantId());
        assertEquals("A1", copy.getAppId());
        assertEquals("SECRET", copy.getApiSecret());
        assertEquals("https://openapi.example.com", copy.getGatewayUrl());
        assertEquals("v", copy.getExtras().get("k"));
    }
}
