package com.jointpay.huifu;

public final class HuifuConstants {

    /**
     * 斗拱聚合支付路径（以官方文档为准，可通过 extras.apiPath 覆盖）。
     */
    public static final String DEFAULT_PREPAY_PATH = "/v3/trade/payment/jspay";
    public static final String DEFAULT_QUERY_PATH = "/v3/trade/payment/query";
    public static final String EXTRA_API_PATH = "apiPath";
    public static final String EXTRA_QUERY_PATH = "queryPath";
    public static final String EXTRA_PAY_TYPE = "payType";
    public static final String NOTIFY_SUCCESS_RESPONSE = "success";

    private HuifuConstants() {
    }
}
