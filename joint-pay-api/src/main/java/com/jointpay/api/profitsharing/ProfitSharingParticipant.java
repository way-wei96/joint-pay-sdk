package com.jointpay.api.profitsharing;

import java.util.Map;

/**
 * 分账参与方。
 */
public final class ProfitSharingParticipant {

    private final String participantId;
    private final String merchantId;
    private final String accountNo;
    private final String role;
    private final ProfitSharingMode mode;
    private final long amountCent;
    private final int ratioBps;
    private final Map<String, String> extras;

    private ProfitSharingParticipant(Builder builder) {
        this.participantId = builder.participantId;
        this.merchantId = builder.merchantId;
        this.accountNo = builder.accountNo;
        this.role = builder.role;
        this.mode = builder.mode == null ? ProfitSharingMode.FIXED_AMOUNT : builder.mode;
        this.amountCent = builder.amountCent;
        this.ratioBps = builder.ratioBps;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public String getParticipantId() {
        return participantId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getRole() {
        return role;
    }

    public ProfitSharingMode getMode() {
        return mode;
    }

    public long getAmountCent() {
        return amountCent;
    }

    /** 比例基点，10000 表示 100%。 */
    public int getRatioBps() {
        return ratioBps;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String participantId;
        private String merchantId;
        private String accountNo;
        private String role;
        private ProfitSharingMode mode;
        private long amountCent;
        private int ratioBps;
        private Map<String, String> extras;

        public Builder participantId(String participantId) {
            this.participantId = participantId;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder accountNo(String accountNo) {
            this.accountNo = accountNo;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder mode(ProfitSharingMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder amountCent(long amountCent) {
            this.amountCent = amountCent;
            return this;
        }

        public Builder ratioBps(int ratioBps) {
            this.ratioBps = ratioBps;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public ProfitSharingParticipant build() {
            return new ProfitSharingParticipant(this);
        }
    }
}
