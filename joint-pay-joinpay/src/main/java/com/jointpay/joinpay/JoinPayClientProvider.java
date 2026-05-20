package com.jointpay.joinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.PayClientProvider;

public final class JoinPayClientProvider implements PayClientProvider {

    @Override
    public PayChannel channel() {
        return PayChannel.JOINPAY;
    }

    @Override
    public PayClient create(ChannelConfig config) {
        return new JoinPayClient(config);
    }
}
