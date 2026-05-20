package com.jointpay.huifu;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.profitsharing.ProfitSharingService;
import com.jointpay.api.refund.RefundService;
import com.jointpay.core.StubChannelPayClient;

public final class HuifuPayClient extends StubChannelPayClient {

    public HuifuPayClient(ChannelConfig config) {
        super(config);
    }

    @Override
    protected PayChannel supportedChannel() {
        return PayChannel.HUIFU;
    }

    @Override
    protected PaymentService createPaymentService(String channelName) {
        return new HuifuPaymentService(getConfig());
    }

    @Override
    protected RefundService createRefundService(String channelName) {
        return new HuifuRefundService(getConfig());
    }

    @Override
    protected ProfitSharingService createProfitSharingService(String channelName) {
        return new HuifuProfitSharingService(getConfig());
    }

    @Override
    protected NotifyHandler createNotifyHandler(String channelName) {
        return new HuifuNotifyHandler(getConfig());
    }
}
