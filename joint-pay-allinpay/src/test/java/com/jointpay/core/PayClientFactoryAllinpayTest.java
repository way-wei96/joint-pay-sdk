package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayClientFactoryAllinpayTest {

    @Test
    void createsAllinpayClientWhenModulePresent() {
        var client = PayClientFactory.create(ChannelConfig.builder(PayChannel.ALLINPAY)
                .merchantId("TL001")
                .apiSecret("secret")
                .build());
        assertEquals(PayChannel.ALLINPAY, client.getChannel());
    }
}
