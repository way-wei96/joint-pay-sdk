package com.jointpay.api.refund;

/**
 * 退款查询请求。
 */
public final class RefundQueryRequest {

    private final String outRefundNo;
    private final String channelRefundNo;

    public RefundQueryRequest(String outRefundNo, String channelRefundNo) {
        this.outRefundNo = outRefundNo;
        this.channelRefundNo = channelRefundNo;
    }

    public static RefundQueryRequest byOutRefundNo(String outRefundNo) {
        return new RefundQueryRequest(outRefundNo, null);
    }

    public static RefundQueryRequest byChannelRefundNo(String channelRefundNo) {
        return new RefundQueryRequest(null, channelRefundNo);
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public String getChannelRefundNo() {
        return channelRefundNo;
    }
}
