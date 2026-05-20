package com.jointpay.api.profitsharing;

/**
 * 分账操作结果（发起、撤销、回退等通用）。
 */
public final class ProfitSharingResult {

    private final String outSharingNo;
    private final String channelSharingNo;
    private final ProfitSharingStatus status;

    public ProfitSharingResult(String outSharingNo, String channelSharingNo, ProfitSharingStatus status) {
        this.outSharingNo = outSharingNo;
        this.channelSharingNo = channelSharingNo;
        this.status = status;
    }

    public String getOutSharingNo() {
        return outSharingNo;
    }

    public String getChannelSharingNo() {
        return channelSharingNo;
    }

    public ProfitSharingStatus getStatus() {
        return status;
    }
}
