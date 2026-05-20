package com.jointpay.api.profitsharing;

import java.util.List;

/**
 * 分账查询结果。
 */
public final class ProfitSharingQueryResult {

    private final String outTradeNo;
    private final String outSharingNo;
    private final String channelSharingNo;
    private final ProfitSharingStatus status;
    private final List<ProfitSharingParticipant> participants;

    public ProfitSharingQueryResult(
            String outTradeNo,
            String outSharingNo,
            String channelSharingNo,
            ProfitSharingStatus status,
            List<ProfitSharingParticipant> participants) {
        this.outTradeNo = outTradeNo;
        this.outSharingNo = outSharingNo;
        this.channelSharingNo = channelSharingNo;
        this.status = status;
        this.participants = participants == null ? List.of() : List.copyOf(participants);
    }

    public String getOutTradeNo() {
        return outTradeNo;
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

    public List<ProfitSharingParticipant> getParticipants() {
        return participants;
    }
}
