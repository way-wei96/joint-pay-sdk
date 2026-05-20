package com.jointpay.api.profitsharing;

/**
 * 分账查询请求。
 */
public final class ProfitSharingQueryRequest {

    private final String outSharingNo;
    private final String channelSharingNo;
    private final String outTradeNo;

    public ProfitSharingQueryRequest(String outSharingNo, String channelSharingNo, String outTradeNo) {
        this.outSharingNo = outSharingNo;
        this.channelSharingNo = channelSharingNo;
        this.outTradeNo = outTradeNo;
    }

    public static ProfitSharingQueryRequest byOutSharingNo(String outSharingNo) {
        return new ProfitSharingQueryRequest(outSharingNo, null, null);
    }

    public String getOutSharingNo() {
        return outSharingNo;
    }

    public String getChannelSharingNo() {
        return channelSharingNo;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }
}
