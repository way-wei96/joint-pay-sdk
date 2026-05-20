package com.jointpay.api.payment;

/**
 * 支付基础能力 SPI，由各渠道模块实现。
 * <p>
 * 分账等高阶能力在后续独立接口中扩展，避免本接口膨胀。
 */
public interface PaymentService {

    PrepayResult prepay(PrepayRequest request);

    PayOrderResult createOrder(PayOrderRequest request);

    OrderQueryResult queryOrder(OrderQueryRequest request);
}
