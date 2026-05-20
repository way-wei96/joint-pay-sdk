package com.jointpay.common.refund;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.refund.RefundQueryRequest;
import com.jointpay.api.refund.RefundQueryResult;
import com.jointpay.api.refund.RefundRequest;
import com.jointpay.api.refund.RefundResult;
import com.jointpay.api.refund.RefundService;

/**
 * 渠道退款服务模板：统一参数校验。
 */
public abstract class AbstractChannelRefundService implements RefundService {

    private final String channelName;

    protected AbstractChannelRefundService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public final RefundResult refund(RefundRequest request) {
        validateRefund(request);
        return doRefund(request);
    }

    @Override
    public final RefundQueryResult queryRefund(RefundQueryRequest request) {
        if ((request.getOutRefundNo() == null || request.getOutRefundNo().isBlank())
                && (request.getChannelRefundNo() == null || request.getChannelRefundNo().isBlank())) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outRefundNo 与 channelRefundNo 不能同时为空");
        }
        return doQueryRefund(request);
    }

    protected abstract RefundResult doRefund(RefundRequest request);

    protected abstract RefundQueryResult doQueryRefund(RefundQueryRequest request);

    protected void validateRefund(RefundRequest request) {
        if (request.getOutRefundNo() == null || request.getOutRefundNo().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outRefundNo 不能为空");
        }
        if (request.getRefundAmountCent() <= 0) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "refundAmountCent 必须大于 0");
        }
        boolean hasTradeRef = (request.getOutTradeNo() != null && !request.getOutTradeNo().isBlank())
                || (request.getChannelTradeNo() != null && !request.getChannelTradeNo().isBlank());
        if (!hasTradeRef) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outTradeNo 与 channelTradeNo 不能同时为空");
        }
    }

    protected String requireExtra(RefundRequest request, String key) {
        String value = request.getExtras().get(key);
        if (value == null || value.isBlank()) {
            throw new JointPayException(
                    ErrorCode.INVALID_ARGUMENT,
                    channelName + " 退款缺少扩展参数: " + key);
        }
        return value;
    }
}
