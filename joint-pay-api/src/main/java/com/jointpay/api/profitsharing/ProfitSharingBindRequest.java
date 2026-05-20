package com.jointpay.api.profitsharing;

import java.util.Map;

/**
 * 下单绑定实时分账请求（在预下单/创建订单阶段附带分账方案）。
 */
public final class ProfitSharingBindRequest {

    private final String outTradeNo;
    private final ProfitSharingScheme scheme;
    private final Map<String, String> extras;

    public ProfitSharingBindRequest(String outTradeNo, ProfitSharingScheme scheme, Map<String, String> extras) {
        this.outTradeNo = outTradeNo;
        this.scheme = scheme;
        this.extras = extras == null ? Map.of() : Map.copyOf(extras);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public ProfitSharingScheme getScheme() {
        return scheme;
    }

    public Map<String, String> getExtras() {
        return extras;
    }
}
