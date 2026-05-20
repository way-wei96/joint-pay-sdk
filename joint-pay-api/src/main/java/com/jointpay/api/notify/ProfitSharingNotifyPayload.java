package com.jointpay.api.notify;

import com.jointpay.api.profitsharing.ProfitSharingStatus;

/**
 * 分账结果类回调，解析并验签后的统一载荷。
 */
public final class ProfitSharingNotifyPayload {

    private final String outTradeNo;
    private final String outSharingNo;
    private final String channelSharingNo;
    private final ProfitSharingStatus status;

    public ProfitSharingNotifyPayload(
            String outTradeNo,
            String outSharingNo,
            String channelSharingNo,
            ProfitSharingStatus status) {
        this.outTradeNo = outTradeNo;
        this.outSharingNo = outSharingNo;
        this.channelSharingNo = channelSharingNo;
        this.status = status;
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
}
