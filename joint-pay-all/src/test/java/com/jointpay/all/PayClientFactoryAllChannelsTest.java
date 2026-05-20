package com.jointpay.all;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.PayClientFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PayClientFactoryAllChannelsTest {

    @Test
    void allChannelProvidersOnClasspath() {
        for (PayChannel channel : PayChannel.values()) {
            var client = PayClientFactory.create(ChannelConfig.builder(channel)
                    .merchantId("M_" + channel.name())
                    .build());
            assertEquals(channel, client.getChannel());
            assertNotNull(client.payment());
            assertNotNull(client.refund());
            assertNotNull(client.notifyHandler());
            assertNotNull(client.profitSharing());
        }
    }
}
