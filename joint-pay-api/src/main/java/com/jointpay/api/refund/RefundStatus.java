package com.jointpay.api.refund;

/**
 * 归一化后的退款状态。
 */
public enum RefundStatus {

    PROCESSING,
    SUCCESS,
    FAILED,
    CLOSED,
    UNKNOWN
}
