package com.jointpay.example;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.core.PayClientFactory;
import com.jointpay.core.notify.NotifySupport;

import java.util.Map;

/**
 * 回调解析示例：在 Web 框架中把 query/body 填入 {@link NotifyRawRequest} 后交给 SDK。
 */
public final class NotifyHandlerDemo {

    private NotifyHandlerDemo() {
    }

    public static void main(String[] args) {
        ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("YOUR_MERCHANT_NO")
                .apiSecret("YOUR_MD5_SECRET")
                .publicKey("PLATFORM_RSA_PUBLIC_KEY_FOR_OPENAPI_NOTIFY")
                .build();

        var client = PayClientFactory.create(config);

        NotifyRawRequest raw = NotifyRawRequest.builder()
                .params(Map.of(
                        "mchAcctOrderNo", "SHARE001",
                        "mchOrderNo", "ORDER001",
                        "fundStatus", "4",
                        "sign", "FROM_CHANNEL"))
                .build();

        NotifyParseResult result = NotifySupport.parse(client, raw);
        System.out.println("通知类型: " + result.getType());
        if (result.getType() == NotifyType.PAY) {
            System.out.println("支付单: " + result.getPay().getOutTradeNo());
        } else if (result.getType() == NotifyType.REFUND) {
            System.out.println("退款单: " + result.getRefund().getOutRefundNo());
        } else if (result.getType() == NotifyType.PROFIT_SHARING) {
            System.out.println("分账单: " + result.getProfitSharing().getOutSharingNo());
        }
        System.out.println("应答渠道: " + NotifySupport.ackBody(result));
    }
}
