package com.jointpay.joinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.profitsharing.ProfitSharingService;
import com.jointpay.api.refund.RefundService;
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

    @Override
    protected RefundService createRefundService(String channelName) {
        return new JoinPayRefundService(getConfig());
    }

    @Override
    protected ProfitSharingService createProfitSharingService(String channelName) {
        return new JoinPayProfitSharingService(getConfig());
    }

    @Override
    protected NotifyHandler createNotifyHandler(String channelName) {
        return new JoinPayNotifyHandler(getConfig());
    }
}
