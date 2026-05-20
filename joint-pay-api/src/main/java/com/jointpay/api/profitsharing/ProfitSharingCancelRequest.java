package com.jointpay.api.profitsharing;

import java.util.Map;

/**
 * 分账撤销请求。
 */
public final class ProfitSharingCancelRequest {

    private final String outSharingNo;
    private final String channelSharingNo;
    private final String reason;
    private final Map<String, String> extras;

    private ProfitSharingCancelRequest(Builder builder) {
        this.outSharingNo = builder.outSharingNo;
        this.channelSharingNo = builder.channelSharingNo;
        this.reason = builder.reason;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutSharingNo() {
        return outSharingNo;
    }

    public String getChannelSharingNo() {
        return channelSharingNo;
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
        private String outSharingNo;
        private String channelSharingNo;
        private String reason;
        private Map<String, String> extras;

        public Builder outSharingNo(String outSharingNo) {
            this.outSharingNo = outSharingNo;
            return this;
        }

        public Builder channelSharingNo(String channelSharingNo) {
            this.channelSharingNo = channelSharingNo;
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

        public ProfitSharingCancelRequest build() {
            return new ProfitSharingCancelRequest(this);
        }
    }
}
