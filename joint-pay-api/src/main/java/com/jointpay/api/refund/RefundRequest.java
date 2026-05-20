package com.jointpay.api.refund;

import java.util.Map;

/**
 * 发起退款请求。
 */
public final class RefundRequest {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final String outRefundNo;
    private final long refundAmountCent;
    private final String reason;
    private final Map<String, String> extras;

    private RefundRequest(Builder builder) {
        this.outTradeNo = builder.outTradeNo;
        this.channelTradeNo = builder.channelTradeNo;
        this.outRefundNo = builder.outRefundNo;
        this.refundAmountCent = builder.refundAmountCent;
        this.reason = builder.reason;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public long getRefundAmountCent() {
        return refundAmountCent;
    }

    public String getReason() {
        return reason;
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
        private String outRefundNo;
        private long refundAmountCent;
        private String reason;
        private Map<String, String> extras;

        public Builder outTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder channelTradeNo(String channelTradeNo) {
            this.channelTradeNo = channelTradeNo;
            return this;
        }

        public Builder outRefundNo(String outRefundNo) {
            this.outRefundNo = outRefundNo;
            return this;
        }

        public Builder refundAmountCent(long refundAmountCent) {
            this.refundAmountCent = refundAmountCent;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public RefundRequest build() {
            return new RefundRequest(this);
        }
    }
}
