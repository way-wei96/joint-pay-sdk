package com.jointpay.api.notify;

import com.jointpay.api.payment.PayStatus;

/**
 * 支付成功/状态变更类回调，解析并验签后的统一载荷。
 */
public final class PayNotifyPayload {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final PayStatus status;
    private final long amountCent;

    public PayNotifyPayload(
            String outTradeNo,
            String channelTradeNo,
            PayStatus status,
            long amountCent) {
        this.outTradeNo = outTradeNo;
        this.channelTradeNo = channelTradeNo;
        this.status = status;
        this.amountCent = amountCent;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public PayStatus getStatus() {
        return status;
    }

    public long getAmountCent() {
        return amountCent;
    }
}
