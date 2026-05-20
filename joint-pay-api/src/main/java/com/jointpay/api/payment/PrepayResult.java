package com.jointpay.api.payment;

import java.util.Map;

/**
 * 预下单结果。
 */
public final class PrepayResult {

    private final String prepayId;
    private final String channelTradeNo;
    private final Map<String, String> payParams;

    public PrepayResult(String prepayId, String channelTradeNo, Map<String, String> payParams) {
        this.prepayId = prepayId;
        this.channelTradeNo = channelTradeNo;
        this.payParams = payParams == null ? Map.of() : Map.copyOf(payParams);
    }

    public String getPrepayId() {
        return prepayId;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    /**
     * 调起支付所需参数（如 JSAPI、APP 等场景），由渠道实现填充。
     */
    public Map<String, String> getPayParams() {
        return payParams;
    }
}
