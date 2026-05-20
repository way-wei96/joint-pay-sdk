package com.jointpay.allinpay;

/**
 * 通联支付常量（收银宝 / 综合支付，路径因产品线而异，以商户文档为准）。
 *
 * @see <a href="https://prodoc.allinpay.com/project/12/">收银宝指引</a>
 * @see <a href="https://prodoc.allinpay.com/doc/244/">统一下单对接说明</a>
 */
public final class AllinpayConstants {

    /** 生产网关示例（请按商户签约文档覆盖 {@link com.jointpay.api.config.ChannelConfig#getGatewayUrl()}）。 */
    public static final String DEFAULT_GATEWAY = "https://vsp.allinpay.com";
    public static final String TEST_GATEWAY = "https://test.allinpaygd.com";

    public static final String DOC_HOME = "https://prodoc.allinpay.com/";
    public static final String DOC_SYB = "https://prodoc.allinpay.com/project/12/";

    /** 与 proid/cusid/trxamt 字段风格一致的开放 API 路径（可通过 extras.apiPath 覆盖）。 */
    public static final String DEFAULT_PREPAY_PATH = "/api/pay";
    public static final String DEFAULT_QUERY_PATH = "/api/query";
    public static final String DEFAULT_REFUND_PATH = "/api/refund";
    public static final String DEFAULT_QUERY_REFUND_PATH = "/api/refundquery";

    public static final String EXTRA_API_PATH = "apiPath";
    public static final String EXTRA_QUERY_PATH = "queryPath";
    public static final String EXTRA_PAY_TYPE = "paytype";
    public static final String EXTRA_SIGN_TYPE = "signtype";
    public static final String NOTIFY_SUCCESS_RESPONSE = "success";

    private AllinpayConstants() {
    }
}
