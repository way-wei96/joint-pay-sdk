package com.jointpay.api.profitsharing;

import java.util.Map;

/**
 * 发起分账请求（支付成功后或独立分账单）。
 */
public final class ProfitSharingRequest {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final String outSharingNo;
    private final ProfitSharingScheme scheme;
    private final Map<String, String> extras;

    private ProfitSharingRequest(Builder builder) {
        this.outTradeNo = builder.outTradeNo;
        this.channelTradeNo = builder.channelTradeNo;
        this.outSharingNo = builder.outSharingNo;
        this.scheme = builder.scheme;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public String getOutSharingNo() {
        return outSharingNo;
    }

    public ProfitSharingScheme getScheme() {
        return scheme;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String outTradeNo;
        private String channelTradeNo;
        private String outSharingNo;
        private ProfitSharingScheme scheme;
        private Map<String, String> extras;

        public Builder outTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder channelTradeNo(String channelTradeNo) {
            this.channelTradeNo = channelTradeNo;
            return this;
        }

        public Builder outSharingNo(String outSharingNo) {
            this.outSharingNo = outSharingNo;
            return this;
        }

        public Builder scheme(ProfitSharingScheme scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public ProfitSharingRequest build() {
            return new ProfitSharingRequest(this);
        }
    }
}
