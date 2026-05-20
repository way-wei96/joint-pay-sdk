package com.jointpay.common.profitsharing;

import com.jointpay.api.profitsharing.ProfitSharingScheme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内「下单绑分账」方案存储（单机适用；集群部署请由业务方自行持有方案）。
 */
public final class InMemoryProfitSharingBindStore {

    private static final Map<String, ProfitSharingScheme> BINDINGS = new ConcurrentHashMap<>();

    private InMemoryProfitSharingBindStore() {
    }

    public static void put(String outTradeNo, ProfitSharingScheme scheme) {
        BINDINGS.put(outTradeNo, scheme);
    }

    public static ProfitSharingScheme get(String outTradeNo) {
        return BINDINGS.get(outTradeNo);
    }

    /** 取出并移除，避免重复应用到多笔订单。 */
    public static ProfitSharingScheme take(String outTradeNo) {
        return BINDINGS.remove(outTradeNo);
    }
}
