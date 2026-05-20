package com.jointpay.common.payment;

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
 * 渠道支付服务模板：统一参数校验，子类实现具体 HTTP 对接。
 */
public abstract class AbstractChannelPaymentService implements PaymentService {

    private final String channelName;

    protected AbstractChannelPaymentService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public final PrepayResult prepay(PrepayRequest request) {
        validatePrepay(request);
        return doPrepay(request);
    }

    @Override
    public final PayOrderResult createOrder(PayOrderRequest request) {
        validateCreateOrder(request);
        return doCreateOrder(request);
    }

    @Override
    public final OrderQueryResult queryOrder(OrderQueryRequest request) {
        if ((request.getOutTradeNo() == null || request.getOutTradeNo().isBlank())
                && (request.getChannelTradeNo() == null || request.getChannelTradeNo().isBlank())) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outTradeNo 与 channelTradeNo 不能同时为空");
        }
        return doQueryOrder(request);
    }

    protected abstract PrepayResult doPrepay(PrepayRequest request);

    protected abstract PayOrderResult doCreateOrder(PayOrderRequest request);

    protected abstract OrderQueryResult doQueryOrder(OrderQueryRequest request);

    protected void validatePrepay(PrepayRequest request) {
        requireOutTradeNo(request.getOutTradeNo());
        requirePositiveAmount(request.getAmountCent());
    }

    protected void validateCreateOrder(PayOrderRequest request) {
        requireOutTradeNo(request.getOutTradeNo());
        requirePositiveAmount(request.getAmountCent());
    }

    protected void requireOutTradeNo(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outTradeNo 不能为空");
        }
    }

    protected void requirePositiveAmount(long amountCent) {
        if (amountCent <= 0) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "amountCent 必须大于 0");
        }
    }

    protected String requireExtra(PrepayRequest request, String key) {
        String value = request.getExtras().get(key);
        if (value == null || value.isBlank()) {
            throw new JointPayException(
                    ErrorCode.INVALID_ARGUMENT,
                    channelName + " 预下单缺少扩展参数: " + key);
        }
        return value;
    }

    protected String requireExtra(PayOrderRequest request, String key) {
        String value = request.getExtras().get(key);
        if (value == null || value.isBlank()) {
            throw new JointPayException(
                    ErrorCode.INVALID_ARGUMENT,
                    channelName + " 下单缺少扩展参数: " + key);
        }
        return value;
    }
}
