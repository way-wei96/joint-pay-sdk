package com.jointpay.api.payment;

/**
 * 归一化后的支付订单状态。
 */
public enum PayStatus {

    WAIT_PAY,
    PAYING,
    SUCCESS,
    CLOSED,
    FAILED,
    UNKNOWN
}
