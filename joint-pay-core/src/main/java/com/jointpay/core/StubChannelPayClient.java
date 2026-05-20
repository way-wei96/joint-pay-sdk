package com.jointpay.core;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.refund.RefundService;
import com.jointpay.core.support.UnsupportedNotifyHandler;
import com.jointpay.core.support.UnsupportedPaymentService;
import com.jointpay.core.support.UnsupportedRefundService;

/**
 * 渠道骨架客户端：挂载占位 SPI，便于三家并行迭代真实实现。
 */
public abstract class StubChannelPayClient extends AbstractPayClient {

    private final PaymentService payment;
    private final RefundService refund;
    private final NotifyHandler notifyHandler;

    protected StubChannelPayClient(ChannelConfig config) {
        super(config);
        String channelName = supportedChannel().getDisplayName();
        this.payment = new UnsupportedPaymentService(channelName);
        this.refund = new UnsupportedRefundService(channelName);
        this.notifyHandler = new UnsupportedNotifyHandler(channelName);
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
