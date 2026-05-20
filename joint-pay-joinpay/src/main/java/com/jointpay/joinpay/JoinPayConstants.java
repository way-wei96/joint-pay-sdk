package com.jointpay.joinpay;

public final class JoinPayConstants {

    public static final String DEFAULT_GATEWAY = "https://www.joinpay.com";
    public static final String UNI_PAY_PATH = "/trade/uniPayApi.action";
    public static final String QUERY_ORDER_PATH = "/trade/queryOrder.action";
    public static final String NOTIFY_SUCCESS_RESPONSE = "success";
    public static final String API_VERSION = "2.1";
    public static final String CURRENCY_CNY = "1";

    /** 支付方式，如 ALIPAY_H5、WEIXIN_NATIVE，通过 {@code extras.frpCode} 传入。 */
    public static final String EXTRA_FRP_CODE = "frpCode";
    /** 报备商户号，可通过 {@code extras.tradeMerchantNo} 或 {@link com.jointpay.api.config.ChannelConfig#getAppId()} 传入。 */
    public static final String EXTRA_TRADE_MERCHANT_NO = "tradeMerchantNo";

    private JoinPayConstants() {
    }
}
