package com.jointpay.api.notify;

/**
 * 回调解析结果（按 {@link #type} 取对应载荷）。
 */
public final class NotifyParseResult {

    private final NotifyType type;
    private final PayNotifyPayload pay;
    private final RefundNotifyPayload refund;
    private final ProfitSharingNotifyPayload profitSharing;
    private final String successResponseBody;

    private NotifyParseResult(Builder builder) {
        this.type = builder.type;
        this.pay = builder.pay;
        this.refund = builder.refund;
        this.profitSharing = builder.profitSharing;
        this.successResponseBody = builder.successResponseBody;
    }

    public NotifyType getType() {
        return type;
    }

    public PayNotifyPayload getPay() {
        return pay;
    }

    public RefundNotifyPayload getRefund() {
        return refund;
    }

    public ProfitSharingNotifyPayload getProfitSharing() {
        return profitSharing;
    }

    /**
     * 应答渠道网关的响应体（如 {@code success}），由各渠道实现指定。
     */
    public String getSuccessResponseBody() {
        return successResponseBody;
    }

    public static Builder builder(NotifyType type) {
        return new Builder(type);
    }

    public static final class Builder {
        private final NotifyType type;
        private PayNotifyPayload pay;
        private RefundNotifyPayload refund;
        private ProfitSharingNotifyPayload profitSharing;
        private String successResponseBody;

        private Builder(NotifyType type) {
            this.type = type;
        }

        public Builder pay(PayNotifyPayload pay) {
            this.pay = pay;
            return this;
        }

        public Builder refund(RefundNotifyPayload refund) {
            this.refund = refund;
            return this;
        }

        public Builder profitSharing(ProfitSharingNotifyPayload profitSharing) {
            this.profitSharing = profitSharing;
            return this;
        }

        public Builder successResponseBody(String successResponseBody) {
            this.successResponseBody = successResponseBody;
            return this;
        }

        public NotifyParseResult build() {
            return new NotifyParseResult(this);
        }
    }
}
