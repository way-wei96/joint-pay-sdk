package com.jointpay.huifu;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.PayClientProvider;

public final class HuifuPayClientProvider implements PayClientProvider {

    @Override
    public PayChannel channel() {
        return PayChannel.HUIFU;
    }

    @Override
    public PayClient create(ChannelConfig config) {
        return new HuifuPayClient(config);
    }
}
