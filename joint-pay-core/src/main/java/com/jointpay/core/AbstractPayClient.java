package com.jointpay.core;

import com.jointpay.api.PayChannel;
import com.jointpay.api.PayClient;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.refund.RefundService;

/**
 * {@link PayClient} 基类，负责校验配置与渠道一致性。
 */
public abstract class AbstractPayClient implements PayClient {

    private final ChannelConfig config;

    protected AbstractPayClient(ChannelConfig config) {
        if (config == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "ChannelConfig 不能为空");
        }
        if (config.getChannel() != supportedChannel()) {
            throw new JointPayException(
                    ErrorCode.INVALID_ARGUMENT,
                    "配置渠道 " + config.getChannel() + " 与实现 " + supportedChannel() + " 不一致");
        }
        this.config = config;
    }

    protected abstract PayChannel supportedChannel();

    @Override
    public PayChannel getChannel() {
        return config.getChannel();
    }

    @Override
    public ChannelConfig getConfig() {
        return config;
    }

    @Override
    public abstract PaymentService payment();

    @Override
    public abstract RefundService refund();

    @Override
    public abstract NotifyHandler notifyHandler();
}
