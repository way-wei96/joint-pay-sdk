package com.jointpay.example;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.core.PayClientFactory;

/**
 * 演示同一套 API 切换渠道（需分别配置各渠道真实参数）。
 */
public final class MultiChannelDemo {

    private MultiChannelDemo() {
    }

    public static void main(String[] args) {
        for (PayChannel channel : PayChannel.values()) {
            ChannelConfig config = ChannelConfig.builder(channel)
                    .merchantId("YOUR_" + channel.name() + "_MERCHANT")
                    .build();
            var client = PayClientFactory.create(config);
            System.out.println(channel.getDisplayName() + " -> " + client.getClass().getSimpleName());
        }
    }
}
