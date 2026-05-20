package com.jointpay.api.payment;

/**
 * 订单查询请求（商户单号与渠道单号二选一或同时提供，由实现层决定优先级）。
 */
public final class OrderQueryRequest {

    private final String outTradeNo;
    private final String channelTradeNo;

    public OrderQueryRequest(String outTradeNo, String channelTradeNo) {
        this.outTradeNo = outTradeNo;
        this.channelTradeNo = channelTradeNo;
    }

    public static OrderQueryRequest byOutTradeNo(String outTradeNo) {
        return new OrderQueryRequest(outTradeNo, null);
    }

    public static OrderQueryRequest byChannelTradeNo(String channelTradeNo) {
        return new OrderQueryRequest(null, channelTradeNo);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }
}
