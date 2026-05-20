package com.jointpay.joinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.notify.PayNotifyPayload;
import com.jointpay.api.notify.RefundNotifyPayload;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.api.payment.PayStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 汇聚支付异步通知（通常为 GET 参数，含 {@code hmac} 签名字段）。
 */
public final class JoinPayNotifyHandler implements NotifyHandler {

    private final ChannelConfig config;

    public JoinPayNotifyHandler(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        Map<String, String> params = request.getParams();
        if (params.isEmpty()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付回调参数为空");
        }
        verifySign(params);

        if (isRefundNotify(params)) {
            return parseRefundNotify(params);
        }

        String outTradeNo = params.get("r2_OrderNo");
        String channelTradeNo = params.get("r7_TrxNo");
        PayStatus status = mapNotifyStatus(params.get("r6_Status"));
        long amountCent = yuanToCent(params.get("r3_Amount"));

        PayNotifyPayload payload = new PayNotifyPayload(outTradeNo, channelTradeNo, status, amountCent);
        return NotifyParseResult.builder(NotifyType.PAY)
                .pay(payload)
                .successResponseBody(JoinPayConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private NotifyParseResult parseRefundNotify(Map<String, String> params) {
        String outRefundNo = firstNonBlank(params.get("r3_RefundOrderNo"), params.get("p3_RefundOrderNo"));
        String outTradeNo = params.get("r2_OrderNo");
        RefundStatus status = mapRefundNotifyStatus(params.get("r6_Status"));
        long amountCent = yuanToCent(params.get("r4_RefundAmount"));
        String channelRefundNo = firstNonBlank(params.get("r7_TrxNo"), outRefundNo);

        RefundNotifyPayload payload = new RefundNotifyPayload(
                outTradeNo, outRefundNo, channelRefundNo, status, amountCent);
        return NotifyParseResult.builder(NotifyType.REFUND)
                .refund(payload)
                .successResponseBody(JoinPayConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private static boolean isRefundNotify(Map<String, String> params) {
        return params.containsKey("r3_RefundOrderNo") || params.containsKey("p3_RefundOrderNo");
    }

    private static RefundStatus mapRefundNotifyStatus(String r6Status) {
        if ("100".equals(r6Status)) {
            return RefundStatus.SUCCESS;
        }
        if ("101".equals(r6Status)) {
            return RefundStatus.FAILED;
        }
        return RefundStatus.PROCESSING;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private void verifySign(Map<String, String> params) {
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付回调验签需配置 apiSecret");
        }
        if (!JoinPaySignUtil.verifyNotify(params, secret)) {
            throw new JointPayException(ErrorCode.SIGN_VERIFY_FAILED, "汇聚支付回调验签失败");
        }
    }

    private static PayStatus mapNotifyStatus(String r6Status) {
        if ("100".equals(r6Status)) {
            return PayStatus.SUCCESS;
        }
        if ("101".equals(r6Status)) {
            return PayStatus.FAILED;
        }
        return PayStatus.UNKNOWN;
    }

    private static long yuanToCent(String yuan) {
        if (yuan == null || yuan.isBlank()) {
            return 0L;
        }
        return new BigDecimal(yuan).movePointRight(2).longValue();
    }
}
