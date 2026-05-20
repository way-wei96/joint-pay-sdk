package com.jointpay.api.profitsharing;

/**
 * 「下单绑分账」方案存储（默认进程内实现；集群请由业务方提供实现并注册）。
 */
public interface ProfitSharingBindStore {

    void put(String outTradeNo, ProfitSharingScheme scheme);

    /**
     * 取出并移除，避免重复应用到多笔订单。
     */
    ProfitSharingScheme take(String outTradeNo);
}
