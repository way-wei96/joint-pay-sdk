package com.jointpay.core.support;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.payment.OrderQueryRequest;
import com.jointpay.api.payment.OrderQueryResult;
import com.jointpay.api.payment.PayOrderRequest;
import com.jointpay.api.payment.PayOrderResult;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.payment.PrepayRequest;
import com.jointpay.api.payment.PrepayResult;

/**
 * 占位实现，渠道具体能力接入前统一返回 {@link ErrorCode#CHANNEL_UNSUPPORTED}。
 */
public final class UnsupportedPaymentService implements PaymentService {

    private final String channelName;

    public UnsupportedPaymentService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public PrepayResult prepay(PrepayRequest request) {
        throw unsupported("预下单");
    }

    @Override
    public PayOrderResult createOrder(PayOrderRequest request) {
        throw unsupported("创建支付订单");
    }

    @Override
    public OrderQueryResult queryOrder(OrderQueryRequest request) {
        throw unsupported("订单查询");
    }

    private JointPayException unsupported(String action) {
        return new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                channelName + " " + action + " 尚未实现");
    }
}
