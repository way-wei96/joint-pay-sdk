package com.jointpay.api.profitsharing;

/**
 * 归一化后的分账状态。
 */
public enum ProfitSharingStatus {

    PROCESSING,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED,
    CANCELLED,
    UNKNOWN
}
