package com.jointpay.joinpay;

import com.jointpay.api.profitsharing.ProfitSharingScheme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内临时存储「下单绑分账」方案，供预下单阶段读取（单机场景；集群请业务方自行持有方案）。
 */
public final class JoinPaySharingBindStore {

    private static final Map<String, ProfitSharingScheme> BINDINGS = new ConcurrentHashMap<>();

    private JoinPaySharingBindStore() {
    }

    public static void put(String outTradeNo, ProfitSharingScheme scheme) {
        BINDINGS.put(outTradeNo, scheme);
    }

    public static ProfitSharingScheme get(String outTradeNo) {
        return BINDINGS.get(outTradeNo);
    }

    public static ProfitSharingScheme remove(String outTradeNo) {
        return BINDINGS.remove(outTradeNo);
    }
}
