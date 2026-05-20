package com.jointpay.common.profitsharing;

import com.jointpay.api.profitsharing.ProfitSharingBindStore;
import com.jointpay.api.profitsharing.ProfitSharingScheme;

/**
 * 全局分账绑定存储入口，默认 {@link InMemoryProfitSharingBindStore}。
 */
public final class ProfitSharingBindStores {

    private static volatile ProfitSharingBindStore delegate = new InMemoryProfitSharingBindStore();

    private ProfitSharingBindStores() {
    }

    public static void use(ProfitSharingBindStore store) {
        delegate = store == null ? new InMemoryProfitSharingBindStore() : store;
    }

    public static ProfitSharingBindStore current() {
        return delegate;
    }

    public static void put(String outTradeNo, ProfitSharingScheme scheme) {
        delegate.put(outTradeNo, scheme);
    }

    public static ProfitSharingScheme take(String outTradeNo) {
        return delegate.take(outTradeNo);
    }
}
