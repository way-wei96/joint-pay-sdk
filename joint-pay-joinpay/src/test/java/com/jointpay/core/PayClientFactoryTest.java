package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayClientFactoryTest {

    @Test
    void createsJoinPayClientWhenModulePresent() {
        ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("M1")
                .build();

        PayClient client = PayClientFactory.create(config);
        assertEquals(PayChannel.JOINPAY, client.getChannel());
        assertEquals("M1", client.getConfig().getMerchantId());
    }

    @Test
    void rejectsNullConfig() {
        JointPayException ex = assertThrows(JointPayException.class, () -> PayClientFactory.create(null));
        assertEquals(ErrorCode.INVALID_ARGUMENT, ex.getErrorCode());
    }
}
