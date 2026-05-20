package com.jointpay.joinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.core.StubChannelPayClient;

public final class JoinPayClient extends StubChannelPayClient {

    public JoinPayClient(ChannelConfig config) {
        super(config);
    }

    @Override
    protected PayChannel supportedChannel() {
        return PayChannel.JOINPAY;
    }

    @Override
    protected PaymentService createPaymentService(String channelName) {
        return new JoinPayPaymentService(getConfig());
    }
}
