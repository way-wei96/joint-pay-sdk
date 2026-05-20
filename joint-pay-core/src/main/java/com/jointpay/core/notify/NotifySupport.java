package com.jointpay.core.notify;

import com.jointpay.api.PayClient;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.notify.PayNotifyPayload;
import com.jointpay.api.notify.ProfitSharingNotifyPayload;
import com.jointpay.api.notify.RefundNotifyPayload;

/**
 * 回调解析便捷入口：Web 层将原始请求交给 {@link PayClient#notifyHandler()} 后的常用封装。
 */
public final class NotifySupport {

    private NotifySupport() {
    }

    public static NotifyParseResult parse(PayClient client, NotifyRawRequest request) {
        return client.notifyHandler().parse(request);
    }

    /**
     * 应答渠道网关的响应体（如 {@code success}），未配置时返回空字符串。
     */
    public static String ackBody(NotifyParseResult result) {
        String body = result.getSuccessResponseBody();
        return body == null ? "" : body;
    }

    public static PayNotifyPayload requirePay(NotifyParseResult result) {
        if (result.getType() != NotifyType.PAY || result.getPay() == null) {
            throw new IllegalStateException("期望支付回调，实际为: " + result.getType());
        }
        return result.getPay();
    }

    public static RefundNotifyPayload requireRefund(NotifyParseResult result) {
        if (result.getType() != NotifyType.REFUND || result.getRefund() == null) {
            throw new IllegalStateException("期望退款回调，实际为: " + result.getType());
        }
        return result.getRefund();
    }

    public static ProfitSharingNotifyPayload requireProfitSharing(NotifyParseResult result) {
        if (result.getType() != NotifyType.PROFIT_SHARING || result.getProfitSharing() == null) {
            throw new IllegalStateException("期望分账回调，实际为: " + result.getType());
        }
        return result.getProfitSharing();
    }
}
