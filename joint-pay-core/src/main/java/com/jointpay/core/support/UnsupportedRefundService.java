package com.jointpay.core.support;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.refund.RefundQueryRequest;
import com.jointpay.api.refund.RefundQueryResult;
import com.jointpay.api.refund.RefundRequest;
import com.jointpay.api.refund.RefundResult;
import com.jointpay.api.refund.RefundService;

public final class UnsupportedRefundService implements RefundService {

    private final String channelName;

    public UnsupportedRefundService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public RefundResult refund(RefundRequest request) {
        throw unsupported("发起退款");
    }

    @Override
    public RefundQueryResult queryRefund(RefundQueryRequest request) {
        throw unsupported("退款查询");
    }

    private JointPayException unsupported(String action) {
        return new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                channelName + " " + action + " 尚未实现");
    }
}
