package com.jointpay.allinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.PayClientProvider;

public final class AllinpayClientProvider implements PayClientProvider {

    @Override
    public PayChannel channel() {
        return PayChannel.ALLINPAY;
    }

    @Override
    public PayClient create(ChannelConfig config) {
        return new AllinpayClient(config);
    }
}
