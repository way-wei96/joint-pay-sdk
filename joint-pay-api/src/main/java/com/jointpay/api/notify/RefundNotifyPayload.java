package com.jointpay.api.notify;

import com.jointpay.api.refund.RefundStatus;

/**
 * 退款结果类回调，解析并验签后的统一载荷。
 */
public final class RefundNotifyPayload {

    private final String outTradeNo;
    private final String outRefundNo;
    private final String channelRefundNo;
    private final RefundStatus status;
    private final long refundAmountCent;

    public RefundNotifyPayload(
            String outTradeNo,
            String outRefundNo,
            String channelRefundNo,
            RefundStatus status,
            long refundAmountCent) {
        this.outTradeNo = outTradeNo;
        this.outRefundNo = outRefundNo;
        this.channelRefundNo = channelRefundNo;
        this.status = status;
        this.refundAmountCent = refundAmountCent;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public String getChannelRefundNo() {
        return channelRefundNo;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public long getRefundAmountCent() {
        return refundAmountCent;
    }
}
