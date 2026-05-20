package com.jointpay.example;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.config.ChannelExtras;
import com.jointpay.api.profitsharing.ProfitSharingBindRequest;
import com.jointpay.api.profitsharing.ProfitSharingParticipant;
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.core.PayClientFactory;
import com.jointpay.api.payment.PrepayRequest;

import java.util.List;
import java.util.Map;

/**
 * 最小可运行示例：请替换为沙箱/生产真实参数后执行 {@link #main}。
 */
public final class QuickStart {

    private QuickStart() {
    }

    public static void main(String[] args) {
        ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("YOUR_MERCHANT_NO")
                .apiSecret("YOUR_MD5_SECRET")
                .appId("YOUR_TRADE_MERCHANT_NO")
                .privateKey("YOUR_RSA_PRIVATE_KEY_FOR_OPENAPI")
                .publicKey("PLATFORM_RSA_PUBLIC_KEY_FOR_OPENAPI_NOTIFY")
                .extras(Map.of("openApiGateway", "https://api.huilianlink.com"))
                .build();

        var client = PayClientFactory.create(config);

        ProfitSharingScheme scheme = new ProfitSharingScheme(
                "DEMO_SCHEME",
                List.of(ProfitSharingParticipant.builder()
                        .participantId("SUB_01")
                        .accountNo("SUB_ACCOUNT_NO")
                        .amountCent(1L)
                        .role("子商户分润")
                        .build()),
                Map.of());

        client.profitSharing().bindOnOrder(new ProfitSharingBindRequest("ORDER_DEMO_001", scheme, Map.of()));

        var prepay = PrepayRequest.builder()
                .outTradeNo("ORDER_DEMO_001")
                .amountCent(100L)
                .subject("演示商品")
                .notifyUrl("https://your.domain/pay/notify")
                .extras(Map.of(ChannelExtras.JoinPay.FRP_CODE, "ALIPAY_H5"))
                .build();

        System.out.println("预下单结果: " + client.payment().prepay(prepay));
    }
}
