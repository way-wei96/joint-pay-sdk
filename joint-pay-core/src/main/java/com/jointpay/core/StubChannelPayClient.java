package com.jointpay.core;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.refund.RefundService;
import com.jointpay.core.support.UnsupportedNotifyHandler;
import com.jointpay.core.support.UnsupportedPaymentService;
import com.jointpay.core.support.UnsupportedRefundService;

/**
 * 渠道骨架客户端：子类可覆盖 {@link #createPaymentService()} 等挂载真实实现。
 */
public abstract class StubChannelPayClient extends AbstractPayClient {

    private final PaymentService payment;
    private final RefundService refund;
    private final NotifyHandler notifyHandler;

    protected StubChannelPayClient(ChannelConfig config) {
        super(config);
        String channelName = supportedChannel().getDisplayName();
        this.payment = createPaymentService(channelName);
        this.refund = createRefundService(channelName);
        this.notifyHandler = createNotifyHandler(channelName);
    }

    protected PaymentService createPaymentService(String channelName) {
        return new UnsupportedPaymentService(channelName);
    }

    protected RefundService createRefundService(String channelName) {
        return new UnsupportedRefundService(channelName);
    }

    protected NotifyHandler createNotifyHandler(String channelName) {
        return new UnsupportedNotifyHandler(channelName);
    }

    @Override
    public PaymentService payment() {
        return payment;
    }

    @Override
    public RefundService refund() {
        return refund;
    }

    @Override
    public NotifyHandler notifyHandler() {
        return notifyHandler;
    }
}
