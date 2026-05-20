package com.jointpay.api.payment;

import java.util.Map;

/**
 * 预下单请求（各渠道字段差异由实现层适配）。
 */
public final class PrepayRequest {

    private final String outTradeNo;
    private final long amountCent;
    private final String subject;
    private final String notifyUrl;
    private final Map<String, String> extras;

    private PrepayRequest(Builder builder) {
        this.outTradeNo = builder.outTradeNo;
        this.amountCent = builder.amountCent;
        this.subject = builder.subject;
        this.notifyUrl = builder.notifyUrl;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public long getAmountCent() {
        return amountCent;
    }

    public String getSubject() {
        return subject;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String outTradeNo;
        private long amountCent;
        private String subject;
        private String notifyUrl;
        private Map<String, String> extras;

        public Builder outTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder amountCent(long amountCent) {
            this.amountCent = amountCent;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder notifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public PrepayRequest build() {
            return new PrepayRequest(this);
        }
    }
}
