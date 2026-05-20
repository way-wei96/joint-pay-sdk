package com.jointpay.api.refund;

/**
 * 退款查询结果。
 */
public final class RefundQueryResult {

    private final String outTradeNo;
    private final String outRefundNo;
    private final String channelRefundNo;
    private final RefundStatus status;
    private final long refundAmountCent;

    public RefundQueryResult(
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
