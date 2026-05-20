package com.jointpay.api;

/**
 * 支持的支付渠道。
 */
public enum PayChannel {

    JOINPAY("汇聚支付"),
    HUIFU("汇付天下"),
    ALLINPAY("通联支付");

    private final String displayName;

    PayChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
