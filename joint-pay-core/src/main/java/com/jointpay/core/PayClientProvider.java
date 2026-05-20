package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;

/**
 * 渠道客户端 SPI，各渠道模块通过 {@link java.util.ServiceLoader} 注册。
 */
public interface PayClientProvider {

    PayChannel channel();

    PayClient create(ChannelConfig config);
}
