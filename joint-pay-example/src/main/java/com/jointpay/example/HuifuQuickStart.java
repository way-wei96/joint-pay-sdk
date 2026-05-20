package com.jointpay.example;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.config.ChannelExtras;
import com.jointpay.api.payment.PrepayRequest;
import com.jointpay.core.PayClientFactory;

import java.util.Map;

/**
 * 汇付斗拱配置示例（需替换为控台「开发者信息」中的真实参数）。
 *
 * @see <a href="https://paas.huifu.com/open/doc/api/">斗拱 API 文档</a>
 */
public final class HuifuQuickStart {

    private HuifuQuickStart() {
    }

    public static void main(String[] args) {
        ChannelConfig config = ChannelConfig.builder(PayChannel.HUIFU)
                .merchantId("YOUR_HUIFU_ID")
                .apiKey("YOUR_SYS_ID")
                .appId("YOUR_PRODUCT_ID")
                .privateKey("YOUR_RSA_PRIVATE_KEY")
                .publicKey("PLATFORM_RSA_PUBLIC_KEY")
                .gatewayUrl("https://api.huifu.com")
                .build();

        var client = PayClientFactory.create(config);
        var result = client.payment().prepay(PrepayRequest.builder()
                .outTradeNo("HF_ORDER_001")
                .amountCent(100L)
                .subject("演示商品")
                .notifyUrl("https://your.domain/huifu/notify")
                .extras(Map.of(ChannelExtras.Huifu.TRADE_TYPE, "T_MINIAPP"))
                .build());
        System.out.println(result);
    }
}
