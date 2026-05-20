package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.ServiceLoader;

/**
 * 按渠道配置创建 {@link PayClient}。
 * <p>
 * 须在 classpath 中引入对应渠道模块（如 {@code joint-pay-joinpay}），否则无法创建客户端。
 */
public final class PayClientFactory {

    private static final Map<PayChannel, PayClientProvider> PROVIDERS = loadProviders();

    private PayClientFactory() {
    }

    public static PayClient create(ChannelConfig config) {
        if (config == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "ChannelConfig 不能为空");
        }
        PayClientProvider provider = PROVIDERS.get(config.getChannel());
        if (provider == null) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_UNSUPPORTED,
                    "未找到 " + config.getChannel().getDisplayName()
                            + " 的实现，请确认已引入对应渠道 Maven 模块");
        }
        return provider.create(config);
    }

    /** 当前 classpath 已注册（已引入对应 Maven 模块）的渠道。 */
    public static Set<PayChannel> supportedChannels() {
        return PROVIDERS.keySet();
    }

    public static boolean isSupported(PayChannel channel) {
        return PROVIDERS.containsKey(channel);
    }

    private static Map<PayChannel, PayClientProvider> loadProviders() {
        Map<PayChannel, PayClientProvider> map = new EnumMap<>(PayChannel.class);
        for (PayClientProvider provider : ServiceLoader.load(PayClientProvider.class)) {
            PayChannel channel = provider.channel();
            if (map.containsKey(channel)) {
                throw new IllegalStateException("重复的 PayClientProvider: " + channel);
            }
            map.put(channel, provider);
        }
        return Map.copyOf(map);
    }
}
