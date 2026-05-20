package com.jointpay.api.payment;

import java.util.Map;

/**
 * 订单查询请求（商户单号与渠道单号可组合提供，由实现层决定优先级）。
 */
public final class OrderQueryRequest {

    private final String outTradeNo;
    private final String channelTradeNo;
    private final Map<String, String> extras;

    private OrderQueryRequest(Builder builder) {
        this.outTradeNo = builder.outTradeNo;
        this.channelTradeNo = builder.channelTradeNo;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public static OrderQueryRequest byOutTradeNo(String outTradeNo) {
        return builder().outTradeNo(outTradeNo).build();
    }

    public static OrderQueryRequest byChannelTradeNo(String channelTradeNo) {
        return builder().channelTradeNo(channelTradeNo).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String outTradeNo;
        private String channelTradeNo;
        private Map<String, String> extras;

        public Builder outTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
            return this;
        }

        public Builder channelTradeNo(String channelTradeNo) {
            this.channelTradeNo = channelTradeNo;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public OrderQueryRequest build() {
            return new OrderQueryRequest(this);
        }
    }
}
