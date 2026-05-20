package com.jointpay.core;

import com.jointpay.api.PayChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayClientFactorySupportedChannelsTest {

    @Test
    void onlyJoinPayOnClasspathInThisModule() {
        assertEquals(1, PayClientFactory.supportedChannels().size());
        assertTrue(PayClientFactory.isSupported(PayChannel.JOINPAY));
        assertFalse(PayClientFactory.isSupported(PayChannel.HUIFU));
        assertFalse(PayClientFactory.isSupported(PayChannel.ALLINPAY));
    }
}
