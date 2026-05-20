package com.jointpay.api.refund;

/**
 * 退款能力 SPI，由各渠道模块实现。
 */
public interface RefundService {

    RefundResult refund(RefundRequest request);

    RefundQueryResult queryRefund(RefundQueryRequest request);
}
