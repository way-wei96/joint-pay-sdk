package com.jointpay.common.profitsharing;

import com.jointpay.api.profitsharing.ProfitSharingBindStore;
import com.jointpay.api.profitsharing.ProfitSharingScheme;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内「下单绑分账」方案存储（单机适用；集群部署请 {@link ProfitSharingBindStores#use} 替换）。
 */
public final class InMemoryProfitSharingBindStore implements ProfitSharingBindStore {

    private final Map<String, ProfitSharingScheme> bindings = new ConcurrentHashMap<>();

    @Override
    public void put(String outTradeNo, ProfitSharingScheme scheme) {
        bindings.put(outTradeNo, scheme);
    }

    @Override
    public ProfitSharingScheme take(String outTradeNo) {
        return bindings.remove(outTradeNo);
    }
}
