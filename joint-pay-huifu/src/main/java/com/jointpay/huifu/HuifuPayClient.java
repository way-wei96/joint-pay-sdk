package com.jointpay.huifu;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.StubChannelPayClient;

public final class HuifuPayClient extends StubChannelPayClient {

    public HuifuPayClient(ChannelConfig config) {
        super(config);
    }

    @Override
    protected PayChannel supportedChannel() {
        return PayChannel.HUIFU;
    }
}
