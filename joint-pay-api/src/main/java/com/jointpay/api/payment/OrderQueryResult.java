package com.jointpay.api.payment;

/**
 * 订单查询结果。
 */
public final class OrderQueryResult {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final PayStatus status;
    private final long amountCent;

    public OrderQueryResult(
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
