package com.jointpay.api;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.payment.PaymentService;
import com.jointpay.api.profitsharing.ProfitSharingService;
import com.jointpay.api.refund.RefundService;

/**
 * 渠道客户端顶层契约，聚合各能力 SPI。
 * <p>
 * 具体实现由 core / 各渠道模块提供，业务方仅依赖本接口与 api 模型。
 */
public interface PayClient {

    PayChannel getChannel();

    ChannelConfig getConfig();

    PaymentService payment();

    RefundService refund();

    NotifyHandler notifyHandler();

    ProfitSharingService profitSharing();
}
