package com.jointpay.api.config;

/**
 * 各渠道 {@link ChannelConfig#getExtras()} 常用键名（与渠道模块常量保持一致，便于 IDE 补全）。
 */
public final class ChannelExtras {

    private ChannelExtras() {
    }

    public static final class JoinPay {
        public static final String FRP_CODE = "frpCode";
        public static final String NOTIFY_URL = "notifyUrl";
        public static final String OPEN_API_GATEWAY = "openApiGateway";
        public static final String TRADE_MERCHANT_NO = "tradeMerchantNo";

        private JoinPay() {
        }
    }

    public static final class Huifu {
        public static final String API_PATH = "apiPath";
        public static final String QUERY_PATH = "queryPath";
        public static final String PAY_TYPE = "payType";
        public static final String REFUND_PATH = "refundPath";
        public static final String NOTIFY_URL = "notifyUrl";
        public static final String SUBMIT_PATH = "submitPath";
        public static final String CANCEL_PATH = "cancelPath";
        public static final String ROLLBACK_PATH = "rollbackPath";

        private Huifu() {
        }
    }

    public static final class Allinpay {
        public static final String API_PATH = "apiPath";
        public static final String QUERY_PATH = "queryPath";
        public static final String PAY_TYPE = "paytype";
        public static final String SIGN_TYPE = "signtype";

        private Allinpay() {
        }
    }
}
