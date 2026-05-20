package com.jointpay.api.profitsharing;

/**
 * 分账金额计算方式。
 */
public enum ProfitSharingMode {

    /** 按固定金额（分） */
    FIXED_AMOUNT,
    /** 按比例，使用 {@link ProfitSharingParticipant#getRatioBps()}，10000 = 100% */
    RATIO
}
