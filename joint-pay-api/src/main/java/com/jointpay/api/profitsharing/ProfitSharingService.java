package com.jointpay.api.profitsharing;

/**
 * 分账能力 SPI（项目核心特色），由各渠道模块实现。
 */
public interface ProfitSharingService {

    /**
     * 下单时绑定实时分账方案（预下单/创建订单前调用，由业务编排决定时机）。
     */
    void bindOnOrder(ProfitSharingBindRequest request);

    /** 发起分账。 */
    ProfitSharingResult submit(ProfitSharingRequest request);

    /** 查询分账。 */
    ProfitSharingQueryResult query(ProfitSharingQueryRequest request);

    /** 撤销分账。 */
    ProfitSharingResult cancel(ProfitSharingCancelRequest request);

    /** 分账回退。 */
    ProfitSharingResult rollback(ProfitSharingRollbackRequest request);
}
