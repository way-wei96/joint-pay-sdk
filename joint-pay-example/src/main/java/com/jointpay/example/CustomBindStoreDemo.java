package com.jointpay.example;

import com.jointpay.api.profitsharing.ProfitSharingBindStore;
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.common.profitsharing.ProfitSharingBindStores;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 集群场景：用 Redis/DB 等实现 {@link ProfitSharingBindStore} 并注册到 {@link ProfitSharingBindStores}。
 */
public final class CustomBindStoreDemo {

    private CustomBindStoreDemo() {
    }

    public static void main(String[] args) {
        ProfitSharingBindStores.use(new ConcurrentHashMapBindStore());
        System.out.println("已切换为自定义分账绑定存储: " + ProfitSharingBindStores.current().getClass().getSimpleName());
    }

    static final class ConcurrentHashMapBindStore implements ProfitSharingBindStore {
        private final Map<String, ProfitSharingScheme> store = new ConcurrentHashMap<>();

        @Override
        public void put(String outTradeNo, ProfitSharingScheme scheme) {
            store.put(outTradeNo, scheme);
        }

        @Override
        public ProfitSharingScheme take(String outTradeNo) {
            return store.remove(outTradeNo);
        }
    }
}
