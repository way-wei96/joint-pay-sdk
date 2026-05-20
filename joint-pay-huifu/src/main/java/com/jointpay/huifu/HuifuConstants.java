package com.jointpay.huifu;

/**
 * 汇付斗拱常量。
 *
 * @see <a href="https://paas.huifu.com/open/doc/api/">斗拱 API 文档</a>
 * @see <a href="https://spin.cloudpnr.com/topds/paramStandards.html">请求/响应参数规定</a>
 */
public final class HuifuConstants {

    /** 生产网关（可通过 {@link com.jointpay.api.config.ChannelConfig#getGatewayUrl()} 覆盖）。 */
    public static final String DEFAULT_GATEWAY = "https://api.huifu.com";

    public static final String DOC_API = "https://paas.huifu.com/open/doc/api/";
    public static final String DOC_PARAM_STANDARDS = "https://spin.cloudpnr.com/topds/paramStandards.html";

    public static final String DEFAULT_PREPAY_PATH = "/v3/trade/payment/jspay";
    public static final String DEFAULT_QUERY_PATH = "/v3/trade/payment/query";
    public static final String DEFAULT_REFUND_PATH = "/v3/trade/payment/refund";
    public static final String DEFAULT_QUERY_REFUND_PATH = "/v3/trade/payment/refund/query";

    public static final String EXTRA_API_PATH = "apiPath";
    public static final String EXTRA_QUERY_PATH = "queryPath";
    public static final String EXTRA_PAY_TYPE = "payType";
    public static final String EXTRA_TRADE_TYPE = "tradeType";
    public static final String NOTIFY_SUCCESS_RESPONSE = "success";

    private HuifuConstants() {
    }
}
