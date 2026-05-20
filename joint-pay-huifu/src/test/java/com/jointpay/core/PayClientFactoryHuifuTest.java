package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PayClientFactoryHuifuTest {

    @Test
    void createsHuifuClientWhenModulePresent() {
        var client = PayClientFactory.create(ChannelConfig.builder(PayChannel.HUIFU)
                .merchantId("HF001")
                .build());
        assertEquals(PayChannel.HUIFU, client.getChannel());
        assertNotNull(client.payment());
        assertNotNull(client.profitSharing());
    }
}
