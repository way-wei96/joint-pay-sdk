package com.jointpay.api.refund;

/**
 * 发起退款结果。
 */
public final class RefundResult {

    private final String outRefundNo;
    private final String channelRefundNo;
    private final RefundStatus status;

    public RefundResult(String outRefundNo, String channelRefundNo, RefundStatus status) {
        this.outRefundNo = outRefundNo;
        this.channelRefundNo = channelRefundNo;
        this.status = status;
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
}
