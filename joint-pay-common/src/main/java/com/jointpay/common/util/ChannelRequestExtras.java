package com.jointpay.common.util;

import com.jointpay.api.config.ChannelExtras;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 将业务 {@code extras} 合并进渠道请求体，跳过 SDK 路由用键，避免覆盖已组装的必填字段。
 */
public final class ChannelRequestExtras {

    private static final Set<String> ROUTING_KEYS = routingKeys();

    private static Set<String> routingKeys() {
        Set<String> keys = new HashSet<>();
        keys.add(ChannelExtras.JoinPay.FRP_CODE);
        keys.add(ChannelExtras.JoinPay.NOTIFY_URL);
        keys.add(ChannelExtras.JoinPay.OPEN_API_GATEWAY);
        keys.add(ChannelExtras.JoinPay.TRADE_MERCHANT_NO);
        keys.add(ChannelExtras.Huifu.API_PATH);
        keys.add(ChannelExtras.Huifu.QUERY_PATH);
        keys.add(ChannelExtras.Huifu.PAY_TYPE);
        keys.add(ChannelExtras.Huifu.TRADE_TYPE);
        keys.add(ChannelExtras.Huifu.SYS_ID);
        keys.add(ChannelExtras.Huifu.PRODUCT_ID);
        keys.add(ChannelExtras.Huifu.REFUND_PATH);
        keys.add(ChannelExtras.Huifu.NOTIFY_URL);
        keys.add(ChannelExtras.Huifu.SUBMIT_PATH);
        keys.add(ChannelExtras.Huifu.CANCEL_PATH);
        keys.add(ChannelExtras.Huifu.ROLLBACK_PATH);
        keys.add(ChannelExtras.Allinpay.API_PATH);
        keys.add(ChannelExtras.Allinpay.QUERY_PATH);
        keys.add(ChannelExtras.Allinpay.PAY_TYPE);
        keys.add(ChannelExtras.Allinpay.SIGN_TYPE);
        return Set.copyOf(keys);
    }

    private ChannelRequestExtras() {
    }

    public static void mergeInto(Map<String, Object> body, Map<String, String> extras) {
        if (extras == null || extras.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : extras.entrySet()) {
            String key = entry.getKey();
            if (ROUTING_KEYS.contains(key)) {
                continue;
            }
            body.putIfAbsent(key, entry.getValue());
        }
    }

    /** 通联等字符串 Map 请求体的 extras 合并。 */
    public static void mergeIntoStringMap(Map<String, String> params, Map<String, String> extras) {
        if (extras == null || extras.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : extras.entrySet()) {
            String key = entry.getKey();
            if (ROUTING_KEYS.contains(key)) {
                continue;
            }
            params.putIfAbsent(key, entry.getValue());
        }
    }
}
