package com.jointpay.api.profitsharing;

import java.util.Map;

/**
 * 分账回退请求（已分账资金退回）。
 */
public final class ProfitSharingRollbackRequest {

    private final String outSharingNo;
    private final String outRollbackNo;
    private final String participantId;
    private final long rollbackAmountCent;
    private final String reason;
    private final Map<String, String> extras;

    private ProfitSharingRollbackRequest(Builder builder) {
        this.outSharingNo = builder.outSharingNo;
        this.outRollbackNo = builder.outRollbackNo;
        this.participantId = builder.participantId;
        this.rollbackAmountCent = builder.rollbackAmountCent;
        this.reason = builder.reason;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getOutSharingNo() {
        return outSharingNo;
    }

    public String getOutRollbackNo() {
        return outRollbackNo;
    }

    public String getParticipantId() {
        return participantId;
    }

    public long getRollbackAmountCent() {
        return rollbackAmountCent;
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
        private String outRollbackNo;
        private String participantId;
        private long rollbackAmountCent;
        private String reason;
        private Map<String, String> extras;

        public Builder outSharingNo(String outSharingNo) {
            this.outSharingNo = outSharingNo;
            return this;
        }

        public Builder outRollbackNo(String outRollbackNo) {
            this.outRollbackNo = outRollbackNo;
            return this;
        }

        public Builder participantId(String participantId) {
            this.participantId = participantId;
            return this;
        }

        public Builder rollbackAmountCent(long rollbackAmountCent) {
            this.rollbackAmountCent = rollbackAmountCent;
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

        public ProfitSharingRollbackRequest build() {
            return new ProfitSharingRollbackRequest(this);
        }
    }
}
