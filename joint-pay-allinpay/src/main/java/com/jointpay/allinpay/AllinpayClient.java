package com.jointpay.allinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.StubChannelPayClient;

public final class AllinpayClient extends StubChannelPayClient {

    public AllinpayClient(ChannelConfig config) {
        super(config);
    }

    @Override
    protected PayChannel supportedChannel() {
        return PayChannel.ALLINPAY;
    }
}
