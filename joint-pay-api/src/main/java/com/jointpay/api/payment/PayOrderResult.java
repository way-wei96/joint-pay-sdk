package com.jointpay.api.payment;

import java.util.Map;

/**
 * 创建支付订单结果。
 */
public final class PayOrderResult {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final PayStatus status;
    private final Map<String, String> payParams;

    public PayOrderResult(
            String outTradeNo,
            String channelTradeNo,
            PayStatus status,
            Map<String, String> payParams) {
        this.outTradeNo = outTradeNo;
        this.channelTradeNo = channelTradeNo;
        this.status = status;
        this.payParams = payParams == null ? Map.of() : Map.copyOf(payParams);
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

    public Map<String, String> getPayParams() {
        return payParams;
    }
}
