package com.jointpay.allinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.refund.RefundService;
import com.jointpay.core.StubChannelPayClient;

public final class AllinpayClient extends StubChannelPayClient {

    public AllinpayClient(ChannelConfig config) {
        super(config);
    }

    @Override
    protected PayChannel supportedChannel() {
        return PayChannel.ALLINPAY;
    }

    @Override
    protected PaymentService createPaymentService(String channelName) {
        return new AllinpayPaymentService(getConfig());
    }

    @Override
    protected RefundService createRefundService(String channelName) {
        return new AllinpayRefundService(getConfig());
    }

    @Override
    protected NotifyHandler createNotifyHandler(String channelName) {
        return new AllinpayNotifyHandler(getConfig());
    }
}
