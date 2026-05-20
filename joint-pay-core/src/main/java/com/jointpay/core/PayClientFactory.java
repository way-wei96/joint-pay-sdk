package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;

/**
 * 按渠道配置创建 {@link PayClient}。
 * <p>
 * 各渠道模块接入后在此注册具体实现类。
 */
public final class PayClientFactory {

    private PayClientFactory() {
    }

    public static PayClient create(ChannelConfig config) {
        if (config == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "ChannelConfig 不能为空");
        }
        PayChannel channel = config.getChannel();
        throw new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                channel.getDisplayName() + " 渠道实现尚未接入，敬请期待");
    }
}
